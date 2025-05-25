package me.myot233.booksystem.service;

import me.myot233.booksystem.entity.Notification;
import me.myot233.booksystem.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 通知服务
 */
@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * 创建并发送通知
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @param type 类型
     * @param bookId 相关图书ID（可选）
     * @return 创建的通知
     */
    public Notification createAndSendNotification(Long userId, String title, String content, 
                                                Notification.NotificationType type, Long bookId) {
        // 创建通知
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBookId(bookId);
        
        // 保存到数据库
        notification = notificationRepository.save(notification);
        
        // 通过WebSocket发送实时通知
        sendRealTimeNotification(userId, notification);
        
        return notification;
    }
    
    /**
     * 发送实时通知
     * @param userId 用户ID
     * @param notification 通知对象
     */
    public void sendRealTimeNotification(Long userId, Notification notification) {
        // 发送给特定用户
        messagingTemplate.convertAndSendToUser(
            userId.toString(), 
            "/queue/notifications", 
            notification
        );
        
        // 同时发送未读通知数量
        long unreadCount = getUnreadNotificationCount(userId);
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/unread-count",
            unreadCount
        );
    }
    
    /**
     * 广播系统通知给所有用户
     * @param title 标题
     * @param content 内容
     */
    public void broadcastSystemNotification(String title, String content) {
        // 这里可以获取所有用户并发送通知
        // 为简化，我们发送到公共频道
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(Notification.NotificationType.SYSTEM_MESSAGE);
        
        messagingTemplate.convertAndSend("/topic/system-notifications", notification);
    }
    
    /**
     * 获取用户的所有通知
     * @param userId 用户ID
     * @return 通知列表
     */
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
    
    /**
     * 获取用户的未读通知
     * @param userId 用户ID
     * @return 未读通知列表
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreateTimeDesc(userId);
    }
    
    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean markAsRead(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            if (notification.getUserId().equals(userId)) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
                
                // 发送更新后的未读数量
                long unreadCount = getUnreadNotificationCount(userId);
                messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/unread-count",
                    unreadCount
                );
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * 标记用户所有通知为已读
     * @param userId 用户ID
     */
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        
        // 发送更新后的未读数量（应该是0）
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/unread-count",
            0L
        );
    }
    
    /**
     * 发送新书到达通知
     * @param bookTitle 图书标题
     * @param bookId 图书ID
     */
    public void sendNewBookNotification(String bookTitle, Long bookId) {
        String title = "新书到达";
        String content = String.format("新书《%s》已到达图书馆，欢迎借阅！", bookTitle);
        
        // 广播给所有用户（实际应用中可能需要根据用户偏好过滤）
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(Notification.NotificationType.NEW_BOOK);
        notification.setBookId(bookId);
        
        messagingTemplate.convertAndSend("/topic/new-books", notification);
    }
    
    /**
     * 发送借阅到期提醒
     * @param userId 用户ID
     * @param bookTitle 图书标题
     * @param bookId 图书ID
     * @param daysLeft 剩余天数
     */
    public void sendReturnReminder(Long userId, String bookTitle, Long bookId, int daysLeft) {
        String title = "归还提醒";
        String content = String.format("您借阅的图书《%s》还有%d天到期，请及时归还。", bookTitle, daysLeft);
        
        createAndSendNotification(userId, title, content, 
            Notification.NotificationType.RETURN_REMINDER, bookId);
    }
    
    /**
     * 发送逾期提醒
     * @param userId 用户ID
     * @param bookTitle 图书标题
     * @param bookId 图书ID
     * @param overdueDays 逾期天数
     */
    public void sendOverdueReminder(Long userId, String bookTitle, Long bookId, int overdueDays) {
        String title = "逾期提醒";
        String content = String.format("您借阅的图书《%s》已逾期%d天，请尽快归还！", bookTitle, overdueDays);
        
        createAndSendNotification(userId, title, content, 
            Notification.NotificationType.OVERDUE_REMINDER, bookId);
    }
}
