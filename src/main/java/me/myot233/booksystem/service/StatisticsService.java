package me.myot233.booksystem.service;

import me.myot233.booksystem.entity.Book;
import me.myot233.booksystem.repository.BookRepository;
import me.myot233.booksystem.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Optional;

/**
 * 统计服务类
 */
@Service
public class StatisticsService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private BookRepository bookRepository;

    private static final String STATS_PREFIX = "stats:";
    private static final String DAILY_STATS_PREFIX = "daily_stats:";
    private static final String USER_ACTIVITY_PREFIX = "user_activity:";
    private static final String BOOK_STATS_PREFIX = "book_stats:";
    private static final String HOT_BOOKS_KEY = "hot_books";

    /**
     * 获取系统统计信息
     * @return 统计信息Map
     */
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 今日借阅统计
        stats.put("todayBorrowCount", getTodayBorrowCount());

        // 在线用户数
        stats.put("onlineUserCount", getOnlineUserCount());

        // 热门图书
        stats.put("hotBooks", getHotBooks(5));

        // 本周借阅统计
        stats.put("weeklyBorrowCount", getWeeklyBorrowCount());

        // 本月借阅统计
        stats.put("monthlyBorrowCount", getMonthlyBorrowCount());

        return stats;
    }

    /**
     * 获取本周借阅统计
     * @return 本周借阅总数
     */
    public Long getWeeklyBorrowCount() {
        String weekKey = STATS_PREFIX + "week:" + getWeekOfYear();
        Object count = redisUtil.get(weekKey);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    /**
     * 获取本月借阅统计
     * @return 本月借阅总数
     */
    public Long getMonthlyBorrowCount() {
        String monthKey = STATS_PREFIX + "month:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Object count = redisUtil.get(monthKey);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    /**
     * 增加周统计
     */
    public void incrementWeeklyBorrowCount() {
        String weekKey = STATS_PREFIX + "week:" + getWeekOfYear();
        redisUtil.incr(weekKey, 1);
        redisUtil.expire(weekKey, 86400 * 30); // 30天过期
    }

    /**
     * 增加月统计
     */
    public void incrementMonthlyBorrowCount() {
        String monthKey = STATS_PREFIX + "month:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        redisUtil.incr(monthKey, 1);
        redisUtil.expire(monthKey, 86400 * 365); // 1年过期
    }

    /**
     * 获取最近7天的借阅统计
     * @return 最近7天的借阅数据
     */
    public Map<String, Long> getRecentSevenDaysStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateKey = DAILY_STATS_PREFIX + date.toString();
            Object count = redisUtil.get(dateKey);
            stats.put(date.toString(), count != null ? Long.valueOf(count.toString()) : 0L);
        }

        return stats;
    }

    /**
     * 增加每日借阅统计
     */
    public void incrementDailyBorrowCount() {
        String todayKey = DAILY_STATS_PREFIX + LocalDate.now().toString();
        redisUtil.incr(todayKey, 1);
        redisUtil.expire(todayKey, 86400 * 30); // 30天过期
    }

    /**
     * 记录用户活动
     * @param userId 用户ID
     * @param activity 活动类型
     */
    public void recordUserActivity(Long userId, String activity) {
        String activityKey = USER_ACTIVITY_PREFIX + userId + ":" + LocalDate.now();
        redisUtil.incr(activityKey, 1);
        redisUtil.expire(activityKey, 86400 * 30); // 30天过期

        // 记录活动类型统计
        String typeKey = STATS_PREFIX + "activity:" + activity + ":" + LocalDate.now();
        redisUtil.incr(typeKey, 1);
        redisUtil.expire(typeKey, 86400 * 30);
    }

    /**
     * 获取用户活跃度排行
     * @param limit 返回数量限制
     * @return 活跃用户列表
     */
    public List<Map<String, Object>> getActiveUsers(int limit) {
        String activeUsersKey = "active_users:" + LocalDate.now();
        Set<Object> userIds = redisTemplate.opsForZSet().reverseRange(activeUsersKey, 0, limit - 1);

        List<Map<String, Object>> activeUsers = new ArrayList<>();
        if (userIds != null) {
            for (Object userId : userIds) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", userId);
                Double score = redisTemplate.opsForZSet().score(activeUsersKey, userId);
                userInfo.put("activityCount", score != null ? score.intValue() : 0);
                activeUsers.add(userInfo);
            }
        }

        return activeUsers;
    }

    /**
     * 更新用户活跃度
     * @param userId 用户ID
     */
    public void updateUserActivity(Long userId) {
        String activeUsersKey = "active_users:" + LocalDate.now();
        redisTemplate.opsForZSet().incrementScore(activeUsersKey, userId.toString(), 1);
        redisUtil.expire(activeUsersKey, 86400); // 24小时过期
    }

    /**
     * 获取图书分类统计
     * @return 分类统计数据
     */
    public Map<String, Long> getCategoryStatistics() {
        String categoryStatsKey = STATS_PREFIX + "categories";
        Map<Object, Object> categoryStats = redisUtil.hmget(categoryStatsKey);

        Map<String, Long> result = new HashMap<>();
        if (categoryStats != null && !categoryStats.isEmpty()) {
            for (Map.Entry<Object, Object> entry : categoryStats.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    try {
                        result.put(entry.getKey().toString(), Long.valueOf(entry.getValue().toString()));
                    } catch (NumberFormatException e) {
                        // 忽略无效的数值
                    }
                }
            }
        }

        return result;
    }

    /**
     * 更新图书分类统计
     * @param category 分类
     */
    public void updateCategoryStatistics(String category) {
        String categoryStatsKey = STATS_PREFIX + "categories";
        redisUtil.hincr(categoryStatsKey, category, 1);
        redisUtil.expire(categoryStatsKey, 86400 * 7); // 7天过期
    }

    /**
     * 清除过期统计数据
     */
    public void cleanExpiredStats() {
        // 清除30天前的日统计数据
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        String expiredKey = DAILY_STATS_PREFIX + thirtyDaysAgo.toString();
        redisUtil.del(expiredKey);

        // 清除过期的用户活动数据
        String expiredActivityKey = USER_ACTIVITY_PREFIX + "*:" + thirtyDaysAgo.toString();
        Set<String> expiredKeys = redisTemplate.keys(expiredActivityKey);
        if (expiredKeys != null && !expiredKeys.isEmpty()) {
            redisTemplate.delete(expiredKeys);
        }
    }

    /**
     * 获取当前周数
     * @return 年份-周数格式的字符串
     */
    private String getWeekOfYear() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int weekOfYear = now.getDayOfYear() / 7 + 1;
        return year + "-W" + String.format("%02d", weekOfYear);
    }

    /**
     * 获取实时系统状态
     * @return 系统状态信息
     */
    public Map<String, Object> getRealtimeSystemStatus() {
        Map<String, Object> status = new HashMap<>();

        // Redis连接状态
        try {
            redisTemplate.opsForValue().get("test");
            status.put("redisStatus", "connected");
        } catch (Exception e) {
            status.put("redisStatus", "disconnected");
        }

        // 缓存命中率（简单模拟）
        status.put("cacheHitRate", calculateCacheHitRate());

        // 当前活跃连接数
        status.put("activeConnections", getOnlineUserCount());

        // 今日系统负载（基于借阅次数）
        Long todayBorrows = getTodayBorrowCount();
        status.put("systemLoad", todayBorrows > 100 ? "high" : todayBorrows > 50 ? "medium" : "low");

        return status;
    }

    /**
     * 计算缓存命中率（简化版本）
     * @return 缓存命中率百分比
     */
    private double calculateCacheHitRate() {
        String hitKey = "cache_hit_count";
        String missKey = "cache_miss_count";

        Long hits = (Long) redisUtil.get(hitKey);
        Long misses = (Long) redisUtil.get(missKey);

        hits = hits != null ? hits : 0L;
        misses = misses != null ? misses : 0L;

        if (hits + misses == 0) {
            return 0.0;
        }

        return (double) hits / (hits + misses) * 100;
    }

    /**
     * 记录缓存命中
     */
    public void recordCacheHit() {
        String hitKey = "cache_hit_count";
        redisUtil.incr(hitKey, 1);
        redisUtil.expire(hitKey, 86400); // 24小时过期
    }

    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss() {
        String missKey = "cache_miss_count";
        redisUtil.incr(missKey, 1);
        redisUtil.expire(missKey, 86400); // 24小时过期
    }

    // ==================== 从BookService移过来的统计方法 ====================

    /**
     * 获取今日借阅统计
     * @return 今日借阅总数
     */
    public Long getTodayBorrowCount() {
        String todayKey = BOOK_STATS_PREFIX + "today_total:" + LocalDate.now();
        Object count = redisUtil.get(todayKey);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    /**
     * 获取在线用户数量
     * @return 在线用户数
     */
    public Long getOnlineUserCount() {
        String onlineUsersKey = "online_users";
        return redisUtil.sGetSetSize(onlineUsersKey);
    }

    /**
     * 获取热门图书排行榜
     * @param limit 返回数量限制
     * @return 热门图书列表
     */
    public List<Book> getHotBooks(int limit) {
        String cacheKey = HOT_BOOKS_KEY + ":" + limit;
        Object cached = redisUtil.get(cacheKey);

        if (cached != null) {
            return (List<Book>) cached;
        }

        // 从Redis有序集合中获取热门图书ID
        Set<Object> hotBookIds = redisTemplate.opsForZSet().reverseRange(HOT_BOOKS_KEY, 0, limit - 1);
        List<Book> hotBooks = new ArrayList<>();

        if (hotBookIds != null) {
            for (Object bookId : hotBookIds) {
                Optional<Book> book = bookRepository.findById(Long.valueOf(bookId.toString()));
                book.ifPresent(hotBooks::add);
            }
        }

        // 缓存结果30分钟
        redisUtil.set(cacheKey, hotBooks, 1800);
        return hotBooks;
    }
}
