package me.myot233.booksystem.controller;

import me.myot233.booksystem.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Redis管理控制器
 */
@RestController
@RequestMapping("/api/redis")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class RedisManagementController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取Redis连接信息
     * @return Redis连接状态
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            // 测试连接
            redisTemplate.opsForValue().get("test");
            info.put("status", "connected");
            info.put("message", "Redis连接正常");

            // 获取数据库大小
            Long dbSize = redisTemplate.getConnectionFactory().getConnection().dbSize();
            info.put("dbSize", dbSize);

        } catch (Exception e) {
            info.put("status", "disconnected");
            info.put("message", "Redis连接失败: " + e.getMessage());
            info.put("dbSize", 0);
        }

        return ResponseEntity.ok(info);
    }

    /**
     * 获取缓存统计信息
     * @return 缓存统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 获取总键数
            Set<String> allKeys = redisTemplate.keys("*");
            stats.put("totalKeys", allKeys != null ? allKeys.size() : 0);

            // 获取不同类型的键数量
            Set<String> bookKeys = redisTemplate.keys("book:*");
            Set<String> statsKeys = redisTemplate.keys("stats:*");
            Set<String> hotBooksKeys = redisTemplate.keys("hot_books*");

            stats.put("bookCacheCount", bookKeys != null ? bookKeys.size() : 0);
            stats.put("statsCacheCount", statsKeys != null ? statsKeys.size() : 0);
            stats.put("hotBooksCacheCount", hotBooksKeys != null ? hotBooksKeys.size() : 0);

        } catch (Exception e) {
            stats.put("error", "获取统计信息失败: " + e.getMessage());
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * 清除所有缓存
     * @return 操作结果
     */
    @DeleteMapping("/flush-all")
    public ResponseEntity<String> flushAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            return ResponseEntity.ok("所有缓存已清除");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除热门图书缓存（解决序列化问题）
     * @return 操作结果
     */
    @DeleteMapping("/flush-hot-books")
    public ResponseEntity<String> flushHotBooksCache() {
        try {
            // 清除热门图书相关的所有缓存
            Set<String> hotBooksKeys = redisTemplate.keys("hot_books*");
            if (hotBooksKeys != null && !hotBooksKeys.isEmpty()) {
                redisTemplate.delete(hotBooksKeys);
            }

            // 清除图书缓存
            Set<String> bookKeys = redisTemplate.keys("book:*");
            if (bookKeys != null && !bookKeys.isEmpty()) {
                redisTemplate.delete(bookKeys);
            }

            return ResponseEntity.ok("热门图书缓存已清除，共清除 " +
                ((hotBooksKeys != null ? hotBooksKeys.size() : 0) +
                 (bookKeys != null ? bookKeys.size() : 0)) + " 个缓存");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("清除缓存失败: " + e.getMessage());
        }
    }
}
