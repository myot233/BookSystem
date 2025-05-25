package me.myot233.booksystem.controller;

import me.myot233.booksystem.entity.Notification;
import me.myot233.booksystem.entity.User;
import me.myot233.booksystem.service.NotificationService;
import me.myot233.booksystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前用户的所有通知
     * @param userDetails 当前登录用户
     * @return 通知列表
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<User> userOpt = userService.getUserByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Notification> notifications = notificationService.getUserNotifications(userOpt.get().getId());
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 获取当前用户的未读通知
     * @param userDetails 当前登录用户
     * @return 未读通知列表
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<User> userOpt = userService.getUserByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Notification> notifications = notificationService.getUnreadNotifications(userOpt.get().getId());
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * 获取当前用户的未读通知数量
     * @param userDetails 当前登录用户
     * @return 未读通知数量
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<User> userOpt = userService.getUserByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        long count = notificationService.getUnreadNotificationCount(userOpt.get().getId());
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @param userDetails 当前登录用户
     * @return 操作结果
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long notificationId,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<User> userOpt = userService.getUserByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        boolean success = notificationService.markAsRead(notificationId, userOpt.get().getId());
        Map<String, String> response = new HashMap<>();
        
        if (success) {
            response.put("message", "标记成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "标记失败");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 标记所有通知为已读
     * @param userDetails 当前登录用户
     * @return 操作结果
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<User> userOpt = userService.getUserByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        notificationService.markAllAsRead(userOpt.get().getId());
        Map<String, String> response = new HashMap<>();
        response.put("message", "所有通知已标记为已读");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 发送系统广播通知（仅管理员）
     * @param request 通知请求
     * @param userDetails 当前登录用户
     * @return 操作结果
     */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, String>> broadcastNotification(@RequestBody BroadcastRequest request,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<User> userOpt = userService.getUserByUsername(userDetails.getUsername());
        if (userOpt.isEmpty() || !"ROLE_ADMIN".equals(userOpt.get().getRole())) {
            return ResponseEntity.status(403).build();
        }
        
        notificationService.broadcastSystemNotification(request.getTitle(), request.getContent());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "系统通知发送成功");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 广播通知请求类
     */
    public static class BroadcastRequest {
        private String title;
        private String content;
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}
