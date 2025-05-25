package me.myot233.booksystem.controller;

import me.myot233.booksystem.entity.Notification;
import me.myot233.booksystem.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket消息控制器
 */
@Controller
public class WebSocketController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 处理用户连接
     * @param message 连接消息
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/connect")
    public void handleConnect(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        // 处理用户连接逻辑
        String sessionId = headerAccessor.getSessionId();
        // 记录连接日志或其他业务逻辑
    }

    /**
     * 处理用户订阅通知
     * @param userId 用户ID
     * @param principal 用户主体
     */
    @MessageMapping("/subscribe")
    public void subscribeToNotifications(@Payload String userId, Principal principal) {
        // 用户订阅通知，可以在这里记录用户的订阅状态
        // 发送当前未读通知数量
        try {
            Long userIdLong = Long.parseLong(userId);
            long unreadCount = notificationService.getUnreadNotificationCount(userIdLong);
            // 这里可以直接发送给用户
        } catch (NumberFormatException e) {
            // 处理无效的用户ID
        }
    }

    /**
     * 处理心跳消息
     * @param message 心跳消息
     */
    @MessageMapping("/heartbeat")
    public void handleHeartbeat(@Payload String message) {
        // 心跳检测，保持连接活跃
    }
}
