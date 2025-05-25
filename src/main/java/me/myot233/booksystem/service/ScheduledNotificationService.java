package me.myot233.booksystem.service;

import me.myot233.booksystem.entity.Book;
import me.myot233.booksystem.entity.User;
import me.myot233.booksystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时通知服务
 */
@Service
public class ScheduledNotificationService {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 每天检查借阅到期情况（每天上午9点执行）
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkBorrowingDueDates() {
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            List<Book> borrowedBooks = user.getBorrowedBooks();
            
            for (Book book : borrowedBooks) {
                // 这里应该有借阅记录表来记录借阅时间和到期时间
                // 为了演示，我们假设借阅期限是30天
                // 实际应用中需要创建BorrowRecord实体来记录详细的借阅信息
                
                // 模拟检查逻辑
                // 如果还有3天到期，发送提醒
                notificationService.sendReturnReminder(
                    user.getId(), 
                    book.getTitle(), 
                    book.getId(), 
                    3
                );
            }
        }
    }
    
    /**
     * 每小时检查逾期情况
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkOverdueBooks() {
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            List<Book> borrowedBooks = user.getBorrowedBooks();
            
            for (Book book : borrowedBooks) {
                // 模拟检查逾期逻辑
                // 如果已逾期1天，发送逾期提醒
                notificationService.sendOverdueReminder(
                    user.getId(), 
                    book.getTitle(), 
                    book.getId(), 
                    1
                );
            }
        }
    }
    
    /**
     * 每周发送系统维护通知（每周日晚上8点）
     */
    @Scheduled(cron = "0 0 20 * * SUN")
    public void sendWeeklyMaintenance() {
        notificationService.broadcastSystemNotification(
            "系统维护通知",
            "系统将于本周日晚上22:00-24:00进行例行维护，期间可能影响部分功能使用。"
        );
    }
}
