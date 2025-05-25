package me.myot233.booksystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 通知实体类
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 接收通知的用户ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * 通知标题
     */
    @Column(nullable = false)
    private String title;
    
    /**
     * 通知内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    /**
     * 通知类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    /**
     * 是否已读
     */
    @Column(nullable = false)
    private Boolean isRead = false;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime = new Date();
    
    /**
     * 相关的图书ID（可选）
     */
    @Column
    private Long bookId;
    
    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        NEW_BOOK("新书到达"),
        BORROW_REMINDER("借阅提醒"),
        RETURN_REMINDER("归还提醒"),
        OVERDUE_REMINDER("逾期提醒"),
        SYSTEM_MESSAGE("系统消息"),
        BOOK_AVAILABLE("图书可借");
        
        private final String description;
        
        NotificationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
