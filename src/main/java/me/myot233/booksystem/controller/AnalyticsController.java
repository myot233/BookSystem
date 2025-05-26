package me.myot233.booksystem.controller;

import me.myot233.booksystem.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据分析控制器
 * 提供数据分析相关的API接口
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * 获取今日统计数据
     * @return 今日统计
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayStats() {
        Map<String, Object> stats = analyticsService.getTodayStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取热门图书排行
     * @param limit 限制数量，默认10
     * @return 热门图书排行
     */
    @GetMapping("/hot-books")
    public ResponseEntity<Map<String, Object>> getHotBooks(
            @RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> hotBooks = analyticsService.getHotBooks(limit);
        return ResponseEntity.ok(hotBooks);
    }

    /**
     * 获取总体统计数据
     * @return 总体统计
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOverviewStats() {
        Map<String, Object> overview = analyticsService.getOverviewStats();
        return ResponseEntity.ok(overview);
    }

    /**
     * 获取最近几天统计
     * @param days 天数，默认7天
     * @return 最近几天统计
     */
    @GetMapping("/recent-days")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecentDaysStats(
            @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> recentStats = analyticsService.getRecentDaysStats(days);
        return ResponseEntity.ok(recentStats);
    }

    /**
     * 获取分析服务状态
     * @return 服务状态
     */
    @GetMapping("/service-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = analyticsService.getAnalyticsServiceStatus();
        return ResponseEntity.ok(status);
    }
}
