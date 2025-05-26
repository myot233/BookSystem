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
        List<Book> hotBooks = statisticsService.getHotBooks(limit);
        return ResponseEntity.ok(hotBooks);
    }

    /**
     * 获取今日借阅统计
     * @return 今日借阅数量
     */
    @GetMapping("/today-borrows")
    public ResponseEntity<Long> getTodayBorrowCount() {
        Long count = statisticsService.getTodayBorrowCount();
        return ResponseEntity.ok(count);
    }

    /**
     * 获取在线用户数量
     * @return 在线用户数
     */
    @GetMapping("/online-users")
    public ResponseEntity<Long> getOnlineUserCount() {
        Long count = statisticsService.getOnlineUserCount();
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


}
