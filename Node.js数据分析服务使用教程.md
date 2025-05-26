# 📊 Node.js数据分析服务使用教程

## 🎯 概述

本教程将指导您如何使用图书管理系统的Node.js数据分析服务。该服务提供实时数据收集、统计分析和监控功能，与Spring Boot主服务无缝集成。

## 🚀 快速开始

### 环境要求
- **Node.js**: 16.0+
- **Redis**: 6.0+
- **网络**: 能访问localhost:3001端口

### 启动服务

#### 1. 启动Redis
```bash
redis-server
```

#### 2. 启动Node.js分析服务
```bash
# 进入服务目录
cd node-analytics

# 安装依赖（首次运行）
npm install

# 启动服务
npm start
```

#### 3. 验证服务状态
```bash
curl http://localhost:3001/api/status
```

预期响应：
```json
{
  "service": "Book Analytics Service",
  "version": "1.0.0",
  "status": "running",
  "redis": "connected",
  "uptime": 123.45
}
```

## 📚 API接口详解

### 🔍 服务状态接口

#### 获取服务状态
```bash
GET /api/status
```

**响应示例：**
```json
{
  "service": "Book Analytics Service",
  "version": "1.0.0",
  "status": "running",
  "redis": "connected",
  "uptime": 3600.5,
  "memory": {
    "rss": 45027328,
    "heapTotal": 13172736,
    "heapUsed": 11631000
  },
  "timestamp": "2025-05-26T08:16:04.000Z"
}
```

### 📥 数据收集接口

#### 1. 收集借阅事件
```bash
POST /api/collect/borrow
Content-Type: application/json

{
  "bookId": 1,
  "userId": 2,
  "timestamp": 1640995200000
}
```

**响应示例：**
```json
{
  "success": true,
  "message": "借阅事件记录成功",
  "timestamp": "2025-05-26T08:16:24.366Z"
}
```

#### 2. 收集归还事件
```bash
POST /api/collect/return
Content-Type: application/json

{
  "bookId": 1,
  "userId": 2,
  "timestamp": 1640995200000
}
```

**响应示例：**
```json
{
  "success": true,
  "message": "归还事件记录成功",
  "timestamp": "2025-05-26T08:16:36.575Z"
}
```

#### 3. 收集登录事件
```bash
POST /api/collect/login
Content-Type: application/json

{
  "userId": 1,
  "username": "testuser"
}
```

**响应示例：**
```json
{
  "success": true,
  "message": "用户登录事件记录成功",
  "timestamp": "2025-05-26T08:16:45.759Z"
}
```

### 📊 统计查询接口

#### 1. 获取今日统计
```bash
GET /api/stats/today
```

**响应示例：**
```json
{
  "date": "2025-05-26",
  "borrows": 3,
  "returns": 1,
  "onlineUsers": 1,
  "dailyLoginUsers": 1,
  "netBorrows": 2
}
```

**字段说明：**
- `date`: 统计日期
- `borrows`: 今日借阅次数
- `returns`: 今日归还次数
- `onlineUsers`: 当前在线用户数
- `dailyLoginUsers`: 今日登录用户数
- `netBorrows`: 净借阅数（借阅-归还）

#### 2. 获取热门图书排行
```bash
GET /api/stats/hot-books?limit=5
```

**响应示例：**
```json
{
  "total": 3,
  "books": [
    {
      "bookId": "3",
      "borrowCount": 1
    },
    {
      "bookId": "2",
      "borrowCount": 1
    },
    {
      "bookId": "1",
      "borrowCount": 1
    }
  ]
}
```

**参数说明：**
- `limit`: 限制返回数量，默认10

#### 3. 获取总体统计
```bash
GET /api/stats/overview
```

**响应示例：**
```json
{
  "totalBorrows": 3,
  "totalReturns": 1,
  "currentOnlineUsers": 1,
  "trackedBooks": 3,
  "systemUptime": 93.5880576,
  "lastUpdated": "2025-05-26T08:17:21.024Z"
}
```

**字段说明：**
- `totalBorrows`: 总借阅次数
- `totalReturns`: 总归还次数
- `currentOnlineUsers`: 当前在线用户数
- `trackedBooks`: 追踪的图书数量
- `systemUptime`: 系统运行时间（秒）

#### 4. 获取最近几天统计
```bash
GET /api/stats/recent-days?days=7
```

**响应示例：**
```json
{
  "period": "最近7天",
  "data": [
    {
      "date": "2025-05-20",
      "borrows": 0,
      "returns": 0,
      "loginUsers": 0
    },
    {
      "date": "2025-05-21",
      "borrows": 5,
      "returns": 3,
      "loginUsers": 8
    },
    {
      "date": "2025-05-26",
      "borrows": 3,
      "returns": 1,
      "loginUsers": 1
    }
  ]
}
```

**参数说明：**
- `days`: 查询天数，默认7天

### 🛠️ 管理接口

#### 手动清理过期数据
```bash
POST /api/admin/cleanup
```

**响应示例：**
```json
{
  "success": true,
  "message": "数据清理完成，清理了 0 个过期键",
  "timestamp": "2025-05-26T08:17:45.901Z"
}
```

## 🧪 实际使用示例

### 示例1：模拟完整的用户行为流程

#### 步骤1：用户登录
```bash
curl -X POST http://localhost:3001/api/collect/login \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "username": "alice"}'
```

#### 步骤2：用户借阅图书
```bash
curl -X POST http://localhost:3001/api/collect/borrow \
  -H "Content-Type: application/json" \
  -d '{"bookId": 101, "userId": 1, "timestamp": 1640995200000}'
```

#### 步骤3：查看今日统计
```bash
curl http://localhost:3001/api/stats/today
```

#### 步骤4：查看热门图书
```bash
curl "http://localhost:3001/api/stats/hot-books?limit=5"
```

### 示例2：批量数据收集测试

#### PowerShell脚本示例：
```powershell
# 模拟多个用户借阅
for ($i=1; $i -le 5; $i++) {
    $body = @{
        bookId = $i
        userId = $i
        timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
    } | ConvertTo-Json

    Invoke-WebRequest -Uri "http://localhost:3001/api/collect/borrow" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $body

    Write-Host "用户 $i 借阅图书 $i"
}

# 查看统计结果
Invoke-WebRequest -Uri "http://localhost:3001/api/stats/today"
```

### 示例3：数据趋势分析

#### 查看最近7天趋势：
```bash
curl "http://localhost:3001/api/stats/recent-days?days=7"
```

#### 分析热门图书：
```bash
curl "http://localhost:3001/api/stats/hot-books?limit=10"
```

## 🔧 与Spring Boot集成

### Spring Boot端调用示例

当您的Spring Boot应用集成了StatisticsService后，可以通过以下接口访问分析数据：

#### 1. 通过Spring Boot获取热门图书排行
```bash
curl http://localhost:8080/api/statistics/hot-books?limit=5
```

#### 2. 通过Spring Boot获取今日借阅统计
```bash
curl http://localhost:8080/api/statistics/today-borrows
```

#### 3. 通过Spring Boot获取在线用户数
```bash
curl http://localhost:8080/api/statistics/online-users
```

#### 4. 通过Spring Boot获取图书分类统计
```bash
curl http://localhost:8080/api/statistics/categories
```

#### 5. 管理员查看系统综合统计
```bash
curl http://localhost:8080/api/statistics/system \
  -H "Authorization: Bearer <admin-jwt-token>"
```

#### 6. 管理员查看最近7天趋势
```bash
curl http://localhost:8080/api/statistics/recent-seven-days \
  -H "Authorization: Bearer <admin-jwt-token>"
```

#### 7. 管理员查看活跃用户排行
```bash
curl http://localhost:8080/api/statistics/active-users \
  -H "Authorization: Bearer <admin-jwt-token>"
```

### 自动数据收集

当Spring Boot应用正常运行时，以下操作会自动发送数据到Node.js分析服务：

- **用户登录** → 自动发送登录事件
- **图书借阅** → 自动发送借阅事件
- **图书归还** → 自动发送归还事件

## 📈 数据监控建议

### 1. 定期检查服务状态
```bash
# 每5分钟检查一次服务状态
*/5 * * * * curl -s http://localhost:3001/api/status | jq '.redis'
```

### 2. 监控关键指标
- **今日借阅数**：异常增长可能表示系统问题
- **在线用户数**：了解系统负载
- **热门图书**：了解用户偏好

### 3. 数据备份
```bash
# 定期备份Redis数据
redis-cli --rdb /backup/analytics-$(date +%Y%m%d).rdb
```

## 🚨 故障排除

### 常见问题

#### 1. 服务无法启动
```bash
# 检查端口占用
netstat -tulpn | grep 3001

# 检查Redis连接
redis-cli ping
```

#### 2. 数据不更新
```bash
# 检查Redis中的数据
redis-cli
> keys stats:*
> get stats:today_borrows:2025-05-26
```

#### 3. 内存使用过高
```bash
# 手动清理过期数据
curl -X POST http://localhost:3001/api/admin/cleanup
```

## 📞 技术支持

如遇到问题，请检查：

1. **服务日志**：查看Node.js服务控制台输出
2. **Redis状态**：确认Redis服务正常运行
3. **网络连接**：确认端口3001可访问
4. **数据格式**：确认请求数据格式正确

## 🎨 高级使用场景

### 场景1：图书馆管理员日常监控

#### 每日工作流程：
```bash
# 1. 检查Spring Boot统计服务状态
curl http://localhost:8080/api/statistics/today-borrows

# 2. 查看今日概况
curl http://localhost:8080/api/statistics/online-users

# 3. 分析热门图书
curl "http://localhost:8080/api/statistics/hot-books?limit=10"

# 4. 查看图书分类统计
curl http://localhost:8080/api/statistics/categories

# 5. 管理员查看系统综合统计
curl http://localhost:8080/api/statistics/system \
  -H "Authorization: Bearer <admin-jwt-token>"

# 6. 管理员查看一周趋势
curl http://localhost:8080/api/statistics/recent-seven-days \
  -H "Authorization: Bearer <admin-jwt-token>"
```

### 场景2：数据分析报告生成

#### 生成周报数据：
```bash
#!/bin/bash
echo "=== 图书馆周报 ===" > weekly_report.txt
echo "生成时间: $(date)" >> weekly_report.txt
echo "" >> weekly_report.txt

# 获取系统综合统计（需要管理员token）
echo "=== 系统综合统计 ===" >> weekly_report.txt
curl -s http://localhost:8080/api/statistics/system \
  -H "Authorization: Bearer <admin-jwt-token>" | jq . >> weekly_report.txt
echo "" >> weekly_report.txt

# 获取热门图书
echo "=== 热门图书TOP10 ===" >> weekly_report.txt
curl -s "http://localhost:8080/api/statistics/hot-books?limit=10" | jq . >> weekly_report.txt
echo "" >> weekly_report.txt

# 获取最近7天数据（需要管理员token）
echo "=== 最近7天趋势 ===" >> weekly_report.txt
curl -s http://localhost:8080/api/statistics/recent-seven-days \
  -H "Authorization: Bearer <admin-jwt-token>" | jq . >> weekly_report.txt
echo "" >> weekly_report.txt

# 获取活跃用户排行（需要管理员token）
echo "=== 活跃用户排行 ===" >> weekly_report.txt
curl -s http://localhost:8080/api/statistics/active-users \
  -H "Authorization: Bearer <admin-jwt-token>" | jq . >> weekly_report.txt
echo "" >> weekly_report.txt

# 获取图书分类统计
echo "=== 图书分类统计 ===" >> weekly_report.txt
curl -s http://localhost:8080/api/statistics/categories | jq . >> weekly_report.txt
```

### 场景3：实时监控仪表板

#### 创建简单的监控脚本：
```bash
#!/bin/bash
while true; do
    clear
    echo "========================================="
    echo "     图书管理系统实时监控面板"
    echo "========================================="
    echo "更新时间: $(date)"
    echo ""

    # 今日统计
    echo "📊 今日统计:"
    TODAY_BORROWS=$(curl -s http://localhost:8080/api/statistics/today-borrows)
    ONLINE_USERS=$(curl -s http://localhost:8080/api/statistics/online-users)
    echo "  今日借阅: $TODAY_BORROWS 次"
    echo "  在线用户: $ONLINE_USERS 人"
    echo ""

    # 热门图书
    echo "🔥 热门图书TOP5:"
    curl -s "http://localhost:8080/api/statistics/hot-books?limit=5" | jq -r '
        .[] | "  \(.title) - \(.author) (借阅\(.borrowed)次)"'
    echo ""

    # 图书分类统计
    echo "📚 图书分类统计:"
    curl -s http://localhost:8080/api/statistics/categories | jq -r '
        to_entries[] | "  \(.key): \(.value) 本"'

    echo ""
    echo "========================================="
    echo "注意：管理员统计需要token，请使用Spring Boot接口"
    sleep 10
done
```

## 🔄 定时任务和自动化

### 1. 定时数据备份

#### Linux Crontab示例：
```bash
# 每天凌晨2点备份数据
0 2 * * * redis-cli --rdb /backup/analytics-$(date +\%Y\%m\%d).rdb

# 每小时检查服务状态
0 * * * * curl -s http://localhost:3001/api/status | jq '.status' | grep -q "running" || systemctl restart analytics-service
```

### 2. 自动报警

#### 创建监控脚本：
```bash
#!/bin/bash
# 检查今日借阅是否异常
TODAY_BORROWS=$(curl -s http://localhost:8080/api/statistics/today-borrows)

if [ "$TODAY_BORROWS" -gt 100 ]; then
    echo "警告：今日借阅数异常高 ($TODAY_BORROWS)" | mail -s "图书系统警报" admin@library.com
fi

# 检查在线用户数是否异常
ONLINE_USERS=$(curl -s http://localhost:8080/api/statistics/online-users)
if [ "$ONLINE_USERS" -gt 50 ]; then
    echo "警告：在线用户数异常高 ($ONLINE_USERS)" | mail -s "系统负载警报" admin@library.com
fi

# 检查Redis服务状态（需要管理员token）
REDIS_STATUS=$(curl -s http://localhost:8080/api/redis/info \
  -H "Authorization: Bearer <admin-jwt-token>" | jq -r '.status')
if [ "$REDIS_STATUS" != "connected" ]; then
    echo "错误：Redis连接状态异常 ($REDIS_STATUS)" | mail -s "Redis故障警报" admin@library.com
fi
```

## 📊 数据可视化建议

### 1. 使用Excel/Google Sheets

#### 数据导出格式：
```bash
# 导出CSV格式的最近7天数据（需要管理员token）
curl -s http://localhost:8080/api/statistics/recent-seven-days \
  -H "Authorization: Bearer <admin-jwt-token>" | \
jq -r 'to_entries[] | [.key, .value] | @csv' > analytics_data.csv

# 导出热门图书数据
curl -s "http://localhost:8080/api/statistics/hot-books?limit=20" | \
jq -r '.[] | [.id, .title, .author, .borrowed] | @csv' > hot_books.csv

# 导出图书分类统计
curl -s http://localhost:8080/api/statistics/categories | \
jq -r 'to_entries[] | [.key, .value] | @csv' > categories.csv
```

### 2. 集成Grafana

#### 创建数据源配置：
```json
{
  "name": "BookStatistics",
  "type": "json",
  "url": "http://localhost:8080/api/statistics/system",
  "access": "proxy",
  "headers": {
    "Authorization": "Bearer <admin-jwt-token>"
  }
}
```

#### 推荐的监控面板：
```json
{
  "dashboard": {
    "title": "图书管理系统监控",
    "panels": [
      {
        "title": "今日借阅统计",
        "type": "stat",
        "targets": [
          {
            "url": "http://localhost:8080/api/statistics/today-borrows"
          }
        ]
      },
      {
        "title": "在线用户数",
        "type": "stat",
        "targets": [
          {
            "url": "http://localhost:8080/api/statistics/online-users"
          }
        ]
      },
      {
        "title": "热门图书排行",
        "type": "table",
        "targets": [
          {
            "url": "http://localhost:8080/api/statistics/hot-books?limit=10"
          }
        ]
      }
    ]
  }
}
```

## 🔒 安全最佳实践

### 1. API访问控制

#### 添加简单的API密钥验证：
```javascript
// 在Node.js服务中添加中间件
app.use('/api/admin/*', (req, res, next) => {
    const apiKey = req.headers['x-api-key'];
    if (apiKey !== process.env.ADMIN_API_KEY) {
        return res.status(401).json({ error: '未授权访问' });
    }
    next();
});
```

### 2. 数据加密

#### Redis数据加密存储：
```bash
# 设置Redis密码
redis-cli CONFIG SET requirepass "your_secure_password"

# 更新Node.js配置
# password: "your_secure_password"
```

## 🚀 性能优化技巧

### 1. 批量数据处理

#### 批量收集事件：
```bash
# 批量发送多个事件
curl -X POST http://localhost:3001/api/collect/batch \
  -H "Content-Type: application/json" \
  -d '{
    "events": [
      {"type": "borrow", "bookId": 1, "userId": 1},
      {"type": "borrow", "bookId": 2, "userId": 2},
      {"type": "return", "bookId": 3, "userId": 3}
    ]
  }'
```

### 2. 缓存优化

#### 使用Redis缓存热点数据：
```bash
# 查看Redis内存使用
redis-cli INFO memory

# 设置过期策略
redis-cli CONFIG SET maxmemory-policy allkeys-lru
```

## 📋 维护检查清单

### 日常检查（每天）
- [ ] 检查服务状态
- [ ] 查看今日统计数据
- [ ] 确认数据收集正常

### 周度检查（每周）
- [ ] 生成周报数据
- [ ] 检查热门图书趋势
- [ ] 清理过期数据

### 月度检查（每月）
- [ ] 备份历史数据
- [ ] 分析月度趋势
- [ ] 优化系统性能

---

*使用教程版本: v1.0*
*最后更新: 2025年1月*
*服务地址: http://localhost:3001*
