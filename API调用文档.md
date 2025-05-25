# 图书管理系统 - API调用文档

## 📚 API调用文档

### 1. 系统信息接口

#### 获取系统信息
```bash
GET /
Content-Type: application/json

# 响应示例
{
  "service": "图书管理系统 API",
  "version": "v1.0.0",
  "status": "running",
  "features": ["JWT认证", "WebSocket实时通知", "图书管理", "用户管理"],
  "endpoints": {
    "GET /api/books": "获取图书列表",
    "POST /api/auth/login": "用户登录",
    "WebSocket /ws": "实时通知连接"
  },
  "testAccounts": {
    "admin": "admin123 (管理员)",
    "user": "user123 (普通用户)"
  }
}
```

#### 健康检查
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

#### 用户登录
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

#### 用户注册
```bash
POST /api/auth/register
Content-Type: application/json

# 请求体
{
  "username": "newuser",
  "password": "password123",
  "realName": "新用户",
  "email": "newuser@example.com"
}

# 响应示例
{
  "message": "注册成功",
  "username": "newuser"
}
```

### 3. 通知相关API

#### 获取用户所有通知
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

#### 获取未读通知
```bash
GET /api/notifications/unread
Authorization: Bearer <jwt-token>
```

#### 获取未读通知数量
```bash
GET /api/notifications/unread/count
Authorization: Bearer <jwt-token>

# 响应示例
{
  "count": 5
}
```

#### 标记通知为已读
```bash
PUT /api/notifications/{notificationId}/read
Authorization: Bearer <jwt-token>

# 响应示例
{
  "message": "标记成功"
}
```

#### 标记所有通知为已读
```bash
PUT /api/notifications/read-all
Authorization: Bearer <jwt-token>

# 响应示例
{
  "message": "所有通知已标记为已读"
}
```

#### 发送系统广播（仅管理员）
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

### 4. 图书相关API

#### 获取所有图书
```bash
GET /api/books/
Content-Type: application/json

# 响应示例
{
  "success": true,
  "data": [
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
}
```

#### 根据ID获取图书
```bash
GET /api/books/{id}
Content-Type: application/json

# 响应示例
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
```

#### 添加新书
```bash
POST /api/books
Content-Type: application/json

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

# 响应示例
{
  "id": 2,
  "title": "人工智能导论",
  "author": "张三",
  "category": "计算机",
  "publisher": "清华大学出版社",
  "isbn": "9787302123456",
  "stock": 5,
  "borrowed": 0,
  "available": 5
}
```

#### 搜索图书
```bash
# 按标题搜索
GET /api/books/search/title?title=三体

# 按作者搜索
GET /api/books/search/author?author=刘慈欣

# 按分类搜索
GET /api/books/search/category?category=科幻
```

### 5. 用户相关API

#### 获取当前用户信息
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

#### 获取所有用户（仅管理员）
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
    "role": "ROLE_ADMIN",
    "createTime": "2025-01-25T10:00:00"
  },
  {
    "id": 2,
    "username": "user",
    "realName": "普通用户",
    "email": "user@example.com",
    "role": "ROLE_USER",
    "createTime": "2025-01-25T10:01:00"
  }
]
```

### 6. WebSocket连接

#### JavaScript客户端示例
```javascript
// 引入依赖
// <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
// <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

// 连接WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

// 使用JWT token连接
const headers = {
    'Authorization': 'Bearer ' + jwtToken
};

stompClient.connect(headers, function(frame) {
    console.log('已连接到WebSocket服务器');
    
    // 订阅个人通知
    stompClient.subscribe('/user/queue/notifications', function(message) {
        const notification = JSON.parse(message.body);
        console.log('收到个人通知:', notification);
        handleNotification(notification);
    });
    
    // 订阅未读数量更新
    stompClient.subscribe('/user/queue/unread-count', function(message) {
        const count = parseInt(message.body);
        console.log('未读通知数量:', count);
        updateUnreadCount(count);
    });
    
    // 订阅系统广播
    stompClient.subscribe('/topic/system-notifications', function(message) {
        const notification = JSON.parse(message.body);
        console.log('收到系统广播:', notification);
        handleSystemMessage(notification);
    });
    
    // 订阅新书通知
    stompClient.subscribe('/topic/new-books', function(message) {
        const notification = JSON.parse(message.body);
        console.log('收到新书通知:', notification);
        handleNewBookNotification(notification);
    });
    
}, function(error) {
    console.error('WebSocket连接失败:', error);
});

// 发送心跳消息（可选）
function sendHeartbeat() {
    if (stompClient && stompClient.connected) {
        stompClient.send('/app/heartbeat', {}, 'ping');
    }
}

// 每30秒发送一次心跳
setInterval(sendHeartbeat, 30000);
```

#### WebSocket消息频道说明
- `/user/queue/notifications` - 个人通知（需要认证）
- `/user/queue/unread-count` - 未读通知数量更新（需要认证）
- `/topic/system-notifications` - 系统广播通知（公开）
- `/topic/new-books` - 新书到达通知（公开）

### 7. 错误处理

#### 常见错误响应
```json
# 401 未授权
{
  "timestamp": "2025-01-25T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token无效或已过期",
  "path": "/api/notifications"
}

# 403 禁止访问
{
  "timestamp": "2025-01-25T10:30:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "权限不足",
  "path": "/api/notifications/broadcast"
}

# 404 资源不存在
{
  "timestamp": "2025-01-25T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "资源不存在",
  "path": "/api/notifications/999"
}

# 409 冲突
{
  "timestamp": "2025-01-25T10:30:00.000+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "用户名已存在",
  "path": "/api/auth/register"
}
```

### 8. 使用示例

#### 完整的登录和获取通知流程
```bash
# 1. 用户登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 响应中获取token
# {"message":"登录成功","username":"admin","token":"eyJhbGciOiJIUzUxMiJ9...","role":"ROLE_ADMIN"}

# 2. 使用token获取通知列表
curl -X GET http://localhost:8080/api/notifications \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."

# 3. 标记通知为已读
curl -X PUT http://localhost:8080/api/notifications/1/read \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."

# 4. 发送系统广播（管理员）
curl -X POST http://localhost:8080/api/notifications/broadcast \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -d '{"title":"系统维护通知","content":"系统将于今晚进行维护"}'
```

#### 添加新书并触发通知
```bash
# 添加新书（会自动触发新书通知）
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "深度学习",
    "author": "Ian Goodfellow",
    "category": "人工智能",
    "publisher": "人民邮电出版社",
    "isbn": "9787115461476",
    "stock": 3,
    "borrowed": 0
  }'
```

### 9. 测试账户信息

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | admin123 | ROLE_ADMIN | 管理员权限，可以发送系统广播、管理用户 |
| user | user123 | ROLE_USER | 普通用户权限，可以查看通知、借阅图书 |

### 10. 服务器信息

- **服务器地址**: `http://localhost:8080`
- **WebSocket端点**: `ws://localhost:8080/ws`
- **API版本**: v1.0.0
- **认证方式**: JWT Bearer Token
- **数据格式**: JSON

---

*文档更新时间: 2025年1月*  
*API版本: v1.0.0*
