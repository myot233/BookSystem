# Book Analytics Service API 文档

## 服务概述

Book Analytics Service 是一个基于 Node.js 和 Redis 的图书管理系统分析服务，提供实时数据收集和统计查询功能。

- **服务端口**: 9999
- **基础URL**: `http://localhost:9999`
- **数据存储**: Redis
- **支持CORS**: 是

## API 接口列表

### 📊 数据收集 API

#### 1. 收集借阅事件
```http
POST /api/collect/borrow
```

**描述**: 记录用户借阅图书的事件

**请求体**:
```json
{
  "bookId": "string",     // 必需 - 图书ID
  "userId": "string",     // 必需 - 用户ID
  "timestamp": "string"   // 可选 - 时间戳
}
```

**响应**:
```json
{
  "success": true,
  "message": "借阅事件记录成功",
  "timestamp": "2024-01-26T10:30:00.000Z"
}
```

**功能**:
- 更新今日借阅统计
- 更新热门图书排行
- 记录用户活动
- 更新总借阅统计

---

#### 2. 收集归还事件
```http
POST /api/collect/return
```

**描述**: 记录用户归还图书的事件

**请求体**:
```json
{
  "bookId": "string",     // 必需 - 图书ID
  "userId": "string",     // 必需 - 用户ID
  "timestamp": "string"   // 可选 - 时间戳
}
```

**响应**:
```json
{
  "success": true,
  "message": "归还事件记录成功",
  "timestamp": "2024-01-26T10:30:00.000Z"
}
```

**功能**:
- 更新今日归还统计
- 更新总归还统计

---

#### 3. 收集用户登录事件
```http
POST /api/collect/login
```

**描述**: 记录用户登录事件

**请求体**:
```json
{
  "userId": "string",     // 必需 - 用户ID
  "username": "string"    // 可选 - 用户名
}
```

**响应**:
```json
{
  "success": true,
  "message": "用户登录事件记录成功",
  "timestamp": "2024-01-26T10:30:00.000Z"
}
```

**功能**:
- 添加到在线用户集合
- 记录今日登录用户

---

### 📈 统计查询 API

#### 4. 获取今日统计
```http
GET /api/stats/today
```

**描述**: 获取今日的各项统计数据

**响应**:
```json
{
  "date": "2024-01-26",
  "borrows": 15,
  "returns": 8,
  "onlineUsers": 12,
  "dailyLoginUsers": 25,
  "netBorrows": 7
}
```

---

#### 5. 获取热门图书排行
```http
GET /api/stats/hot-books?limit=10
```

**描述**: 获取热门图书排行榜

**查询参数**:
- `limit` (可选): 返回数量限制，默认10

**响应**:
```json
{
  "total": 5,
  "books": [
    {
      "bookId": "123",
      "borrowCount": 25
    },
    {
      "bookId": "456",
      "borrowCount": 18
    }
  ]
}
```

---

#### 6. 获取总体统计
```http
GET /api/stats/overview
```

**描述**: 获取系统总体统计信息

**响应**:
```json
{
  "totalBorrows": 1250,
  "totalReturns": 1100,
  "currentOnlineUsers": 12,
  "trackedBooks": 150,
  "systemUptime": 86400.5,
  "lastUpdated": "2024-01-26T10:30:00.000Z"
}
```

---

#### 7. 获取最近几天统计
```http
GET /api/stats/recent-days?days=7
```

**描述**: 获取最近几天的统计数据

**查询参数**:
- `days` (可选): 天数，默认7天

**响应**:
```json
{
  "period": "最近7天",
  "data": [
    {
      "date": "2024-01-20",
      "borrows": 15,
      "returns": 12,
      "loginUsers": 25
    },
    {
      "date": "2024-01-21",
      "borrows": 18,
      "returns": 14,
      "loginUsers": 28
    }
  ]
}
```

---

### 🔧 系统管理 API

#### 8. 获取服务状态
```http
GET /api/status
```

**描述**: 获取服务运行状态和系统信息

**响应**:
```json
{
  "service": "Book Analytics Service",
  "version": "1.0.0",
  "status": "running",
  "redis": "connected",
  "uptime": 86400.5,
  "memory": {
    "rss": 50331648,
    "heapTotal": 20971520,
    "heapUsed": 15728640,
    "external": 1048576
  },
  "timestamp": "2024-01-26T10:30:00.000Z"
}
```

---

#### 9. 清理过期数据
```http
POST /api/admin/cleanup
```

**描述**: 手动清理过期的统计数据

**响应**:
```json
{
  "success": true,
  "message": "数据清理完成，清理了 15 个过期键",
  "timestamp": "2024-01-26T10:30:00.000Z"
}
```

---

## 🕐 定时任务

### 1. 数据清理任务
- **执行时间**: 每天凌晨2点
- **功能**: 清理30天前的过期数据
- **清理内容**: 
  - 每日借阅统计
  - 每日归还统计
  - 每日登录用户记录

### 2. 在线用户清理
- **执行时间**: 每小时
- **功能**: 更新在线用户过期时间，清理离线用户

---

## 📝 错误响应格式

所有API在发生错误时都会返回以下格式的响应：

```json
{
  "error": "错误描述信息"
}
```

常见HTTP状态码：
- `200`: 成功
- `400`: 请求参数错误
- `500`: 服务器内部错误

---

## 🔗 Redis 数据结构

### 键名规范
- `stats:today_borrows:{date}`: 今日借阅统计
- `stats:today_returns:{date}`: 今日归还统计
- `stats:daily_login_users:{date}`: 每日登录用户集合
- `stats:total_borrows`: 总借阅统计
- `stats:total_returns`: 总归还统计
- `hot_books`: 热门图书有序集合
- `online_users`: 在线用户集合
- `active_users`: 活跃用户集合

### 数据过期策略
- 在线用户: 1小时
- 活跃用户: 1小时
- 每日登录用户: 7天
- 历史统计数据: 30天后自动清理

---

## 🚀 使用示例

### 记录借阅事件
```bash
curl -X POST http://localhost:9999/api/collect/borrow \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": "123",
    "userId": "456",
    "timestamp": "2024-01-26T10:30:00.000Z"
  }'
```

### 获取今日统计
```bash
curl http://localhost:9999/api/stats/today
```

### 获取热门图书
```bash
curl http://localhost:9999/api/stats/hot-books?limit=5
```
