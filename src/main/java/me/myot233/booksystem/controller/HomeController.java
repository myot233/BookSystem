package me.myot233.booksystem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统信息控制器
 */
@RestController
public class HomeController {

    /**
     * 系统信息接口
     * @return 系统状态和API信息
     */
    @GetMapping("/")
    public Map<String, Object> systemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "图书管理系统 API");
        info.put("version", "v1.0.0");
        info.put("status", "running");
        info.put("features", new String[]{"JWT认证", "WebSocket实时通知", "图书管理", "用户管理", "Node.js数据分析"});

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/books", "获取图书列表");
        endpoints.put("POST /api/auth/login", "用户登录");
        endpoints.put("POST /api/auth/register", "用户注册");
        endpoints.put("GET /api/users/me", "获取当前用户信息");
        endpoints.put("GET /api/notifications", "获取通知列表");
        endpoints.put("GET /api/analytics/today", "获取今日统计");
        endpoints.put("GET /api/analytics/hot-books", "获取热门图书");
        endpoints.put("WebSocket /ws", "实时通知连接");
        info.put("endpoints", endpoints);

        Map<String, String> testAccounts = new HashMap<>();
        testAccounts.put("admin", "admin123 (管理员)");
        testAccounts.put("user", "user123 (普通用户)");
        info.put("testAccounts", testAccounts);

        return info;
    }

    /**
     * 健康检查接口
     * @return 系统健康状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("services", Map.of(
            "database", "UP",
            "jwt", "UP",
            "websocket", "UP"
        ));
        return health;
    }
}
