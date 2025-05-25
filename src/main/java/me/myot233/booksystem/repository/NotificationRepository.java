package me.myot233.booksystem.repository;

import me.myot233.booksystem.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知数据访问接口
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 根据用户ID查找通知
     * @param userId 用户ID
     * @return 通知列表
     */
    List<Notification> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    /**
     * 根据用户ID查找未读通知
     * @param userId 用户ID
     * @return 未读通知列表
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreateTimeDesc(Long userId);
    
    /**
     * 统计用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    long countByUserIdAndIsReadFalse(Long userId);
    
    /**
     * 标记用户所有通知为已读
     * @param userId 用户ID
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsReadByUserId(@Param("userId") Long userId);
    
    /**
     * 根据通知类型查找通知
     * @param type 通知类型
     * @return 通知列表
     */
    List<Notification> findByType(Notification.NotificationType type);
    
    /**
     * 根据用户ID和通知类型查找通知
     * @param userId 用户ID
     * @param type 通知类型
     * @return 通知列表
     */
    List<Notification> findByUserIdAndType(Long userId, Notification.NotificationType type);
}
