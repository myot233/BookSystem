package me.myot233.booksystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据分析服务客户端
 * 负责与Node.js分析服务通信
 */
@Slf4j
@Service
public class AnalyticsService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${analytics.service.url:http://localhost:3001}")
    private String analyticsServiceUrl;

    /**
     * 发送借阅事件到分析服务
     * @param bookId 图书ID
     * @param userId 用户ID
     */
    public void sendBorrowEvent(Long bookId, Long userId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("bookId", bookId);
            data.put("userId", userId);
            data.put("timestamp", System.currentTimeMillis());

            String url = analyticsServiceUrl + "/api/collect/borrow";
            restTemplate.postForObject(url, data, String.class);
            
            log.info("借阅事件发送成功: 用户{} 借阅图书{}", userId, bookId);
        } catch (Exception e) {
            // 分析服务异常不影响主业务
            log.warn("发送借阅事件失败: {}", e.getMessage());
        }
    }

    /**
     * 发送归还事件到分析服务
     * @param bookId 图书ID
     * @param userId 用户ID
     */
    public void sendReturnEvent(Long bookId, Long userId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("bookId", bookId);
            data.put("userId", userId);
            data.put("timestamp", System.currentTimeMillis());

            String url = analyticsServiceUrl + "/api/collect/return";
            restTemplate.postForObject(url, data, String.class);
            
            log.info("归还事件发送成功: 用户{} 归还图书{}", userId, bookId);
        } catch (Exception e) {
            log.warn("发送归还事件失败: {}", e.getMessage());
        }
    }

    /**
     * 发送用户登录事件到分析服务
     * @param userId 用户ID
     * @param username 用户名
     */
    public void sendLoginEvent(Long userId, String username) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("username", username);
            data.put("timestamp", System.currentTimeMillis());

            String url = analyticsServiceUrl + "/api/collect/login";
            restTemplate.postForObject(url, data, String.class);
            
            log.info("登录事件发送成功: 用户{}({})", userId, username);
        } catch (Exception e) {
            log.warn("发送登录事件失败: {}", e.getMessage());
        }
    }

    /**
     * 获取今日统计数据
     * @return 统计数据
     */
    public Map<String, Object> getTodayStats() {
        try {
            String url = analyticsServiceUrl + "/api/stats/today";
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            return result;
        } catch (Exception e) {
            log.error("获取今日统计失败: {}", e.getMessage());
            return createErrorResponse("获取今日统计失败");
        }
    }

    /**
     * 获取热门图书排行
     * @param limit 限制数量
     * @return 热门图书数据
     */
    public Map<String, Object> getHotBooks(Integer limit) {
        try {
            String url = analyticsServiceUrl + "/api/stats/hot-books";
            if (limit != null) {
                url += "?limit=" + limit;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            return result;
        } catch (Exception e) {
            log.error("获取热门图书失败: {}", e.getMessage());
            return createErrorResponse("获取热门图书失败");
        }
    }

    /**
     * 获取总体统计
     * @return 总体统计数据
     */
    public Map<String, Object> getOverviewStats() {
        try {
            String url = analyticsServiceUrl + "/api/stats/overview";
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            return result;
        } catch (Exception e) {
            log.error("获取总体统计失败: {}", e.getMessage());
            return createErrorResponse("获取总体统计失败");
        }
    }

    /**
     * 获取最近几天统计
     * @param days 天数
     * @return 统计数据
     */
    public Map<String, Object> getRecentDaysStats(Integer days) {
        try {
            String url = analyticsServiceUrl + "/api/stats/recent-days";
            if (days != null) {
                url += "?days=" + days;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            return result;
        } catch (Exception e) {
            log.error("获取最近几天统计失败: {}", e.getMessage());
            return createErrorResponse("获取最近几天统计失败");
        }
    }

    /**
     * 获取分析服务状态
     * @return 服务状态
     */
    public Map<String, Object> getAnalyticsServiceStatus() {
        try {
            String url = analyticsServiceUrl + "/api/status";
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            return result;
        } catch (Exception e) {
            log.error("获取分析服务状态失败: {}", e.getMessage());
            return createErrorResponse("分析服务不可用");
        }
    }

    /**
     * 创建错误响应
     * @param message 错误消息
     * @return 错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}
