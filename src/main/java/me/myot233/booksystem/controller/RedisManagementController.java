package me.myot233.booksystem.controller;

import me.myot233.booksystem.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
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
     * 获取所有键
     * @param pattern 键模式，默认为*
     * @return 键列表
     */
    @GetMapping("/keys")
    public ResponseEntity<Set<String>> getKeys(@RequestParam(defaultValue = "*") String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return ResponseEntity.ok(keys != null ? keys : new HashSet<>());
    }

    /**
     * 获取键值
     * @param key 键名
     * @return 键值
     */
    @GetMapping("/get/{key}")
    public ResponseEntity<Object> getValue(@PathVariable String key) {
        Object value = redisUtil.get(key);
        return ResponseEntity.ok(value);
    }

    /**
     * 设置键值
     * @param key 键名
     * @param value 值
     * @param ttl 过期时间（秒），可选
     * @return 操作结果
     */
    @PostMapping("/set")
    public ResponseEntity<String> setValue(@RequestParam String key, 
                                         @RequestParam String value,
                                         @RequestParam(required = false) Long ttl) {
        boolean success;
        if (ttl != null && ttl > 0) {
            success = redisUtil.set(key, value, ttl);
        } else {
            success = redisUtil.set(key, value);
        }
        
        return ResponseEntity.ok(success ? "设置成功" : "设置失败");
    }

    /**
     * 删除键
     * @param key 键名
     * @return 操作结果
     */
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<String> deleteKey(@PathVariable String key) {
        redisUtil.del(key);
        return ResponseEntity.ok("删除成功");
    }

    /**
     * 批量删除键
     * @param keys 键名列表
     * @return 操作结果
     */
    @DeleteMapping("/delete-batch")
    public ResponseEntity<String> deleteKeys(@RequestBody List<String> keys) {
        redisUtil.del(keys.toArray(new String[0]));
        return ResponseEntity.ok("批量删除成功，共删除 " + keys.size() + " 个键");
    }

    /**
     * 获取键的过期时间
     * @param key 键名
     * @return 过期时间（秒）
     */
    @GetMapping("/ttl/{key}")
    public ResponseEntity<Long> getTtl(@PathVariable String key) {
        long ttl = redisUtil.getExpire(key);
        return ResponseEntity.ok(ttl);
    }

    /**
     * 设置键的过期时间
     * @param key 键名
     * @param seconds 过期时间（秒）
     * @return 操作结果
     */
    @PutMapping("/expire/{key}")
    public ResponseEntity<String> setExpire(@PathVariable String key, @RequestParam long seconds) {
        boolean success = redisUtil.expire(key, seconds);
        return ResponseEntity.ok(success ? "设置过期时间成功" : "设置过期时间失败");
    }

    /**
     * 检查键是否存在
     * @param key 键名
     * @return 是否存在
     */
    @GetMapping("/exists/{key}")
    public ResponseEntity<Boolean> keyExists(@PathVariable String key) {
        boolean exists = redisUtil.hasKey(key);
        return ResponseEntity.ok(exists);
    }

    /**
     * 获取缓存统计信息
     * @return 缓存统计
     */
    @GetMapping("/cache-stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取不同类型的键数量
        Set<String> bookKeys = redisTemplate.keys("book:*");
        Set<String> searchKeys = redisTemplate.keys("book_search:*");
        Set<String> statsKeys = redisTemplate.keys("stats:*");
        Set<String> hotBooksKeys = redisTemplate.keys("hot_books*");
        
        stats.put("bookCacheCount", bookKeys != null ? bookKeys.size() : 0);
        stats.put("searchCacheCount", searchKeys != null ? searchKeys.size() : 0);
        stats.put("statsCacheCount", statsKeys != null ? statsKeys.size() : 0);
        stats.put("hotBooksCacheCount", hotBooksKeys != null ? hotBooksKeys.size() : 0);
        
        // 总键数
        Set<String> allKeys = redisTemplate.keys("*");
        stats.put("totalKeys", allKeys != null ? allKeys.size() : 0);
        
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
     * 清除特定模式的缓存
     * @param pattern 键模式
     * @return 操作结果
     */
    @DeleteMapping("/flush-pattern")
    public ResponseEntity<String> flushByPattern(@RequestParam String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            return ResponseEntity.ok("已清除 " + keys.size() + " 个匹配的缓存");
        } else {
            return ResponseEntity.ok("没有找到匹配的缓存");
        }
    }

    /**
     * 获取热门图书排行数据
     * @return 热门图书排行
     */
    @GetMapping("/hot-books-ranking")
    public ResponseEntity<Set<Object>> getHotBooksRanking() {
        var ranking = redisTemplate.opsForZSet().reverseRangeWithScores("hot_books", 0, -1);
        return ResponseEntity.ok(Collections.singleton(ranking));
    }

    /**
     * 获取在线用户列表
     * @return 在线用户ID列表
     */
    @GetMapping("/online-users")
    public ResponseEntity<Set<Object>> getOnlineUsers() {
        Set<Object> onlineUsers = redisUtil.sGet("online_users");
        return ResponseEntity.ok(onlineUsers != null ? onlineUsers : new HashSet<>());
    }

    /**
     * 手动添加测试数据
     * @return 操作结果
     */
    @PostMapping("/test-data")
    public ResponseEntity<String> addTestData() {
        // 添加一些测试缓存数据
        redisUtil.set("test:string", "Hello Redis", 3600);
        redisUtil.hset("test:hash", "field1", "value1", 3600);
        redisUtil.hset("test:hash", "field2", "value2", 3600);
        redisUtil.sSet("test:set", "member1", "member2", "member3");
        
        // 添加热门图书测试数据
        redisTemplate.opsForZSet().add("hot_books", "1", 10);
        redisTemplate.opsForZSet().add("hot_books", "2", 8);
        redisTemplate.opsForZSet().add("hot_books", "3", 15);
        
        return ResponseEntity.ok("测试数据已添加");
    }

    /**
     * 获取Redis内存使用情况
     * @return 内存使用信息
     */
    @GetMapping("/memory-info")
    public ResponseEntity<Map<String, Object>> getMemoryInfo() {
        Map<String, Object> memoryInfo = new HashMap<>();
        
        try {
            // 这里可以通过Redis INFO命令获取更详细的内存信息
            // 简化版本，只返回基本信息
            Set<String> allKeys = redisTemplate.keys("*");
            memoryInfo.put("totalKeys", allKeys != null ? allKeys.size() : 0);
            memoryInfo.put("estimatedMemoryUsage", "需要Redis INFO命令支持");
            
        } catch (Exception e) {
            memoryInfo.put("error", "获取内存信息失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(memoryInfo);
    }

    /**
     * 测试Redis性能
     * @param operations 操作次数，默认1000
     * @return 性能测试结果
     */
    @PostMapping("/performance-test")
    public ResponseEntity<Map<String, Object>> performanceTest(@RequestParam(defaultValue = "1000") int operations) {
        Map<String, Object> result = new HashMap<>();
        
        long startTime = System.currentTimeMillis();
        
        // 执行SET操作
        for (int i = 0; i < operations; i++) {
            redisUtil.set("perf_test:" + i, "value" + i, 60);
        }
        
        long setTime = System.currentTimeMillis() - startTime;
        
        // 执行GET操作
        startTime = System.currentTimeMillis();
        for (int i = 0; i < operations; i++) {
            redisUtil.get("perf_test:" + i);
        }
        
        long getTime = System.currentTimeMillis() - startTime;
        
        // 清理测试数据
        for (int i = 0; i < operations; i++) {
            redisUtil.del("perf_test:" + i);
        }
        
        result.put("operations", operations);
        result.put("setTimeMs", setTime);
        result.put("getTimeMs", getTime);
        result.put("setOpsPerSecond", operations * 1000.0 / setTime);
        result.put("getOpsPerSecond", operations * 1000.0 / getTime);
        
        return ResponseEntity.ok(result);
    }
}
