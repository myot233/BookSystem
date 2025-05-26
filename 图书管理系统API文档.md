# 图书管理系统 - 完整API调用文档

## 📋 权限说明

- 🔓 **无需认证** - 公开接口，任何人都可以访问
- 🔐 **需要认证** - 需要JWT token，普通用户权限
- 👑 **管理员权限** - 需要管理员JWT token

## 🔑 认证方式

所有需要认证的接口都需要在请求头中添加：
```
Authorization: Bearer <jwt-token>
```

---

## 📚 API接口列表

### 1. 系统信息接口

#### 获取系统信息 🔓
```bash
GET /
Content-Type: application/json

# 响应示例
{
  "service": "图书管理系统 API",
  "version": "v1.0.0",
  "status": "running",
  "features": ["JWT认证", "WebSocket实时通知", "图书管理", "用户管理", "Node.js数据分析"]
}
```

#### 健康检查 🔓
```bash
GET /health
Content-Type: application/json

# 响应示例
{
  "status": "UP",
  "timestamp": 1640995200000,
  "services": {
    "database": "UP",
    "jwt": "UP",
    "websocket": "UP"
  }
}
```

### 2. 认证相关API

#### 用户登录 🔓
```bash
POST /api/auth/login
Content-Type: application/json

# 请求体
{
  "username": "admin",
  "password": "admin123"
}

# 响应示例
{
  "message": "登录成功",
  "username": "admin",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "role": "ROLE_ADMIN"
}
```

#### 用户注册 🔓
```bash
POST /api/auth/register
Content-Type: application/json

# 请求体
{
  "username": "newuser",
  "password": "password123",
  "realName": "新用户",
  "email": "newuser@example.com",
  "phone": "13800138000"
}

# 响应示例
{
  "message": "注册成功",
  "username": "newuser"
}
```

### 3. 图书管理API

#### 获取所有图书 🔓
```bash
GET /api/books/
Content-Type: application/json

# 响应示例
[
  {
    "id": 1,
    "title": "三体",
    "author": "刘慈欣",
    "category": "科幻",
    "publisher": "重庆出版社",
    "isbn": "9787536692930",
    "stock": 10,
    "borrowed": 3,
    "available": 7
  }
]
```

#### 根据ID获取图书 🔓
```bash
GET /api/books/{id}
Content-Type: application/json
```

#### 搜索图书 🔓
```bash
# 按标题搜索
GET /api/books/search/title?title=三体

# 按作者搜索
GET /api/books/search/author?author=刘慈欣

# 按分类搜索
GET /api/books/search/category?category=科幻
```

#### 添加新书 👑
```bash
POST /api/books
Content-Type: application/json
Authorization: Bearer <admin-jwt-token>

# 请求体
{
  "title": "人工智能导论",
  "author": "张三",
  "category": "计算机",
  "publisher": "清华大学出版社",
  "isbn": "9787302123456",
  "stock": 5,
  "borrowed": 0
}
```

#### 更新图书信息 👑
```bash
PUT /api/books/{id}
Content-Type: application/json
Authorization: Bearer <admin-jwt-token>

# 请求体
{
  "title": "人工智能导论（第二版）",
  "author": "张三",
  "category": "计算机",
  "publisher": "清华大学出版社",
  "isbn": "9787302123456",
  "stock": 8,
  "borrowed": 2
}
```

#### 删除图书 👑
```bash
DELETE /api/books/{id}
Authorization: Bearer <admin-jwt-token>

# 响应：204 No Content
```

### 4. 用户个人借阅管理API

#### 获取我的借阅列表 🔐
```bash
GET /api/users/me/books
Authorization: Bearer <jwt-token>

# 响应示例
[
  {
    "id": 1,
    "title": "三体",
    "author": "刘慈欣",
    "category": "科幻"
  }
]
```

#### 我要借阅图书 🔐
```bash
POST /api/users/me/books/{bookId}
Authorization: Bearer <jwt-token>

# 响应示例
{
  "id": 2,
  "username": "user",
  "borrowedBooks": [
    {
      "id": 1,
      "title": "三体"
    }
  ]
}
```

#### 我要归还图书 🔐
```bash
DELETE /api/users/me/books/{bookId}
Authorization: Bearer <jwt-token>

# 响应示例
{
  "id": 2,
  "username": "user",
  "borrowedBooks": []
}
```

### 5. 用户信息API

#### 获取当前用户信息 🔐
```bash
GET /api/users/me
Authorization: Bearer <jwt-token>

# 响应示例
{
  "id": 1,
  "username": "admin",
  "realName": "管理员",
  "email": "admin@example.com",
  "role": "ROLE_ADMIN",
  "createTime": "2025-01-25T10:00:00",
  "lastLoginTime": "2025-01-25T10:30:00"
}
```

### 6. 管理员用户管理API

#### 获取所有用户 👑
```bash
GET /api/users
Authorization: Bearer <admin-jwt-token>

# 响应示例
[
  {
    "id": 1,
    "username": "admin",
    "realName": "管理员",
    "email": "admin@example.com",
    "role": "ROLE_ADMIN"
  }
]
```

#### 获取指定用户信息 👑
```bash
GET /api/users/{id}
Authorization: Bearer <admin-jwt-token>
```

#### 更新用户信息 👑
```bash
PUT /api/users/{id}
Content-Type: application/json
Authorization: Bearer <admin-jwt-token>

# 请求体
{
  "realName": "新姓名",
  "email": "newemail@example.com",
  "phone": "13900139000"
}
```

#### 删除用户 👑
```bash
DELETE /api/users/{id}
Authorization: Bearer <admin-jwt-token>

# 响应：204 No Content
```

### 7. 管理员借阅管理API

#### 查看用户借阅情况 👑
```bash
GET /api/users/{id}/books
Authorization: Bearer <admin-jwt-token>

# 示例：查看用户ID为2的借阅情况
GET /api/users/2/books
Authorization: Bearer <admin-jwt-token>

# 响应示例
[
  {
    "id": 1,
    "title": "三体",
    "author": "刘慈欣",
    "category": "科幻",
    "publisher": "重庆出版社",
    "isbn": "9787536692930"
  },
  {
    "id": 3,
    "title": "算法导论",
    "author": "Thomas H. Cormen",
    "category": "计算机",
    "publisher": "机械工业出版社",
    "isbn": "9787111407010"
  }
]
```

#### 帮用户借阅图书 👑
```bash
POST /api/users/{userId}/books/{bookId}
Authorization: Bearer <admin-jwt-token>

# 示例：帮用户ID为2的用户借阅图书ID为5的图书
POST /api/users/2/books/5
Authorization: Bearer <admin-jwt-token>

# 响应示例
{
  "id": 2,
  "username": "user",
  "realName": "普通用户",
  "email": "user@example.com",
  "role": "ROLE_USER",
  "borrowedBooks": [
    {
      "id": 1,
      "title": "三体"
    },
    {
      "id": 5,
      "title": "新借阅的图书"
    }
  ]
}
```

#### 帮用户归还图书 👑
```bash
DELETE /api/users/{userId}/books/{bookId}
Authorization: Bearer <admin-jwt-token>

# 示例：帮用户ID为2的用户归还图书ID为1的图书
DELETE /api/users/2/books/1
Authorization: Bearer <admin-jwt-token>

# 响应示例
{
  "id": 2,
  "username": "user",
  "realName": "普通用户",
  "email": "user@example.com",
  "role": "ROLE_USER",
  "borrowedBooks": [
    {
      "id": 5,
      "title": "新借阅的图书"
    }
  ]
}
```

### 8. 通知管理API

#### 获取用户通知 🔐
```bash
GET /api/notifications
Authorization: Bearer <jwt-token>

# 响应示例
[
  {
    "id": 1,
    "title": "新书到达",
    "content": "新书《人工智能导论》已到达图书馆",
    "type": "NEW_BOOK",
    "isRead": false,
    "createTime": "2025-01-25T10:30:00",
    "bookId": 123
  }
]
```

#### 获取未读通知数量 🔐
```bash
GET /api/notifications/unread/count
Authorization: Bearer <jwt-token>

# 响应示例
{
  "count": 5
}
```

#### 标记通知已读 🔐
```bash
PUT /api/notifications/{id}/read
Authorization: Bearer <jwt-token>

# 响应示例
{
  "message": "标记成功"
}
```

#### 标记所有通知已读 🔐
```bash
PUT /api/notifications/read-all
Authorization: Bearer <jwt-token>

# 响应示例
{
  "message": "所有通知已标记为已读"
}
```

#### 发送系统广播 👑
```bash
POST /api/notifications/broadcast
Content-Type: application/json
Authorization: Bearer <admin-jwt-token>

# 请求体
{
  "title": "系统维护通知",
  "content": "系统将于今晚22:00-24:00进行维护。"
}

# 响应示例
{
  "message": "系统通知发送成功"
}
```

### 9. 数据分析API

#### 获取今日统计 🔓
```bash
GET /api/analytics/today
Content-Type: application/json

# 响应示例
{
  "date": "2025-01-27",
  "borrows": 15,
  "returns": 8,
  "onlineUsers": 5,
  "dailyLoginUsers": 12,
  "netBorrows": 7
}
```

#### 获取热门图书排行 🔓
```bash
GET /api/analytics/hot-books?limit=10
Content-Type: application/json

# 响应示例
{
  "total": 5,
  "books": [
    {
      "bookId": "1",
      "borrowCount": 25
    },
    {
      "bookId": "3",
      "borrowCount": 18
    },
    {
      "bookId": "2",
      "borrowCount": 12
    }
  ]
}
```

#### 获取总体统计 👑
```bash
GET /api/analytics/overview
Authorization: Bearer <admin-jwt-token>

# 响应示例
{
  "totalBorrows": 156,
  "totalReturns": 134,
  "currentOnlineUsers": 5,
  "trackedBooks": 12,
  "systemUptime": 3600,
  "lastUpdated": "2025-01-27T10:30:00.000Z"
}
```

#### 获取最近几天统计 👑
```bash
GET /api/analytics/recent-days?days=7
Authorization: Bearer <admin-jwt-token>

# 响应示例
{
  "period": "最近7天",
  "data": [
    {
      "date": "2025-01-21",
      "borrows": 12,
      "returns": 8,
      "loginUsers": 15
    },
    {
      "date": "2025-01-22",
      "borrows": 18,
      "returns": 12,
      "loginUsers": 22
    },
    {
      "date": "2025-01-23",
      "borrows": 15,
      "returns": 10,
      "loginUsers": 18
    }
  ]
}
```

#### 获取分析服务状态 👑
```bash
GET /api/analytics/service-status
Authorization: Bearer <admin-jwt-token>

# 响应示例
{
  "service": "Book Analytics Service",
  "version": "1.0.0",
  "status": "running",
  "redis": "connected",
  "uptime": 3600,
  "memory": {
    "rss": 45678592,
    "heapTotal": 20971520,
    "heapUsed": 15234816
  },
  "timestamp": "2025-01-27T10:30:00.000Z"
}
```

---

## 🧪 测试账户

| 用户名 | 密码 | 角色 | 权限说明 |
|--------|------|------|----------|
| admin | admin123 | ROLE_ADMIN | 管理员权限，可以管理图书、用户、发送系统广播 |
| user | user123 | ROLE_USER | 普通用户权限，可以借阅图书、查看通知 |

---

## 🔧 使用流程示例

### 普通用户借阅流程
```bash
# 1. 登录
POST /api/auth/login
{"username": "user", "password": "user123"}

# 2. 查看图书
GET /api/books/

# 3. 借阅图书
POST /api/users/me/books/1
Authorization: Bearer <token>

# 4. 查看我的借阅
GET /api/users/me/books
Authorization: Bearer <token>

# 5. 归还图书
DELETE /api/users/me/books/1
Authorization: Bearer <token>
```

### 管理员管理流程
```bash
# 1. 登录管理员
POST /api/auth/login
Content-Type: application/json
{
  "username": "admin",
  "password": "admin123"
}

# 2. 添加新书
POST /api/books
Content-Type: application/json
Authorization: Bearer <admin-token>
{
  "title": "深度学习",
  "author": "Ian Goodfellow",
  "category": "人工智能",
  "publisher": "人民邮电出版社",
  "isbn": "9787115461476",
  "stock": 3,
  "borrowed": 0
}

# 3. 查看用户借阅情况
GET /api/users/2/books
Authorization: Bearer <admin-token>

# 4. 帮用户借阅图书
POST /api/users/2/books/1
Authorization: Bearer <admin-token>

# 5. 帮用户归还图书
DELETE /api/users/2/books/1
Authorization: Bearer <admin-token>

# 6. 发送系统通知
POST /api/notifications/broadcast
Content-Type: application/json
Authorization: Bearer <admin-token>
{
  "title": "系统维护通知",
  "content": "系统将于今晚22:00-24:00进行维护，请提前保存工作。"
}
```

### 数据分析使用流程
```bash
# 1. 查看今日统计（无需认证）
GET /api/analytics/today

# 2. 查看热门图书排行（无需认证）
GET /api/analytics/hot-books?limit=5

# 3. 管理员查看总体统计
GET /api/analytics/overview
Authorization: Bearer <admin-token>

# 4. 管理员查看最近7天统计
GET /api/analytics/recent-days?days=7
Authorization: Bearer <admin-token>

# 5. 管理员检查分析服务状态
GET /api/analytics/service-status
Authorization: Bearer <admin-token>
```

---

## 🚀 Node.js数据分析服务

### 服务说明
本系统集成了独立的Node.js数据分析服务，提供实时数据收集和统计分析功能。

### 主要功能
- **实时数据收集**: 自动收集用户借阅、归还、登录等行为数据
- **统计分析**: 提供今日统计、热门图书排行、用户活跃度等分析
- **自动清理**: 定时清理过期数据，保持系统性能
- **服务监控**: 提供服务状态监控和健康检查

### 技术架构
```
Spring Boot (8080) ←→ Node.js Analytics (3001)
       ↓                         ↓
     MySQL                   Redis (共享)
```

### 数据流程
1. 用户在Spring Boot系统中进行借阅、归还、登录等操作
2. Spring Boot自动发送事件数据到Node.js分析服务
3. Node.js服务实时处理数据并存储到Redis
4. 前端可通过API获取实时统计数据

### 启动分析服务
```bash
# 进入Node.js服务目录
cd node-analytics

# 安装依赖
npm install

# 启动服务
npm start
```

分析服务将在 `http://localhost:3001` 启动

---

*文档更新时间: 2025年1月*
*API版本: v1.0.0*
*服务器地址: http://localhost:8080*
*分析服务地址: http://localhost:3001*
