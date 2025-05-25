package me.myot233.booksystem.controller;

import me.myot233.booksystem.entity.Book;
import me.myot233.booksystem.service.BookService;
import me.myot233.booksystem.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private BookService bookService;

    /**
     * 获取系统统计信息
     * @return 统计信息
     */
    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> stats = statisticsService.getSystemStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取热门图书排行榜
     * @param limit 返回数量限制，默认10
     * @return 热门图书列表
     */
    @GetMapping("/hot-books")
    public ResponseEntity<List<Book>> getHotBooks(@RequestParam(defaultValue = "10") int limit) {
        List<Book> hotBooks = bookService.getHotBooks(limit);
        return ResponseEntity.ok(hotBooks);
    }

    /**
     * 获取今日借阅统计
     * @return 今日借阅数量
     */
    @GetMapping("/today-borrows")
    public ResponseEntity<Long> getTodayBorrowCount() {
        Long count = bookService.getTodayBorrowCount();
        return ResponseEntity.ok(count);
    }

    /**
     * 获取在线用户数量
     * @return 在线用户数
     */
    @GetMapping("/online-users")
    public ResponseEntity<Long> getOnlineUserCount() {
        Long count = bookService.getOnlineUserCount();
        return ResponseEntity.ok(count);
    }

    /**
     * 获取最近7天借阅统计
     * @return 最近7天的借阅数据
     */
    @GetMapping("/recent-seven-days")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getRecentSevenDaysStats() {
        Map<String, Long> stats = statisticsService.getRecentSevenDaysStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取活跃用户排行
     * @param limit 返回数量限制，默认10
     * @return 活跃用户列表
     */
    @GetMapping("/active-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getActiveUsers(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> activeUsers = statisticsService.getActiveUsers(limit);
        return ResponseEntity.ok(activeUsers);
    }

    /**
     * 获取图书分类统计
     * @return 分类统计数据
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Long>> getCategoryStatistics() {
        Map<String, Long> stats = statisticsService.getCategoryStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取本周借阅统计
     * @return 本周借阅数量
     */
    @GetMapping("/weekly-borrows")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getWeeklyBorrowCount() {
        Long count = statisticsService.getWeeklyBorrowCount();
        return ResponseEntity.ok(count);
    }

    /**
     * 获取本月借阅统计
     * @return 本月借阅数量
     */
    @GetMapping("/monthly-borrows")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getMonthlyBorrowCount() {
        Long count = statisticsService.getMonthlyBorrowCount();
        return ResponseEntity.ok(count);
    }

    /**
     * 获取图书借阅次数
     * @param bookId 图书ID
     * @return 借阅次数
     */
    @GetMapping("/book-borrow-count/{bookId}")
    public ResponseEntity<Long> getBookBorrowCount(@PathVariable Long bookId) {
        Long count = bookService.getBookBorrowCount(bookId);
        return ResponseEntity.ok(count);
    }

    /**
     * 获取实时系统状态
     * @return 系统状态信息
     */
    @GetMapping("/system-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRealtimeSystemStatus() {
        Map<String, Object> status = statisticsService.getRealtimeSystemStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * 清除过期统计数据
     * @return 操作结果
     */
    @DeleteMapping("/clean-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cleanExpiredStats() {
        statisticsService.cleanExpiredStats();
        return ResponseEntity.ok("过期统计数据已清除");
    }

    /**
     * 手动记录用户活动（用于测试）
     * @param userId 用户ID
     * @param activity 活动类型
     * @return 操作结果
     */
    @PostMapping("/record-activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> recordUserActivity(@RequestParam Long userId, @RequestParam String activity) {
        statisticsService.recordUserActivity(userId, activity);
        statisticsService.updateUserActivity(userId);
        return ResponseEntity.ok("用户活动已记录");
    }

    /**
     * 手动更新图书分类统计（用于测试）
     * @param category 分类名称
     * @return 操作结果
     */
    @PostMapping("/update-category-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateCategoryStatistics(@RequestParam String category) {
        statisticsService.updateCategoryStatistics(category);
        return ResponseEntity.ok("分类统计已更新");
    }

    /**
     * 获取缓存统计信息
     * @return 缓存相关统计
     */
    @GetMapping("/cache-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        Map<String, Object> cacheStats = Map.of(
            "hitRate", statisticsService.getRealtimeSystemStatus().get("cacheHitRate"),
            "redisStatus", statisticsService.getRealtimeSystemStatus().get("redisStatus")
        );
        return ResponseEntity.ok(cacheStats);
    }

    /**
     * 手动记录缓存命中（用于测试）
     * @return 操作结果
     */
    @PostMapping("/cache-hit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> recordCacheHit() {
        statisticsService.recordCacheHit();
        return ResponseEntity.ok("缓存命中已记录");
    }

    /**
     * 手动记录缓存未命中（用于测试）
     * @return 操作结果
     */
    @PostMapping("/cache-miss")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> recordCacheMiss() {
        statisticsService.recordCacheMiss();
        return ResponseEntity.ok("缓存未命中已记录");
    }
}
