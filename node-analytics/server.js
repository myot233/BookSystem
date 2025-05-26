const express = require('express');
const redis = require('redis');
const cron = require('node-cron');
const cors = require('cors');

const app = express();
const PORT = 3001;

// 中间件
app.use(cors());
app.use(express.json());

// Redis客户端
const redisClient = redis.createClient({
  socket: {
    host: 'localhost',
    port: 6379
  }
  // 如果Redis设置了密码，添加下面的配置
  // password: '123456'
});

// Redis连接
redisClient.connect().catch(console.error);

redisClient.on('connect', () => {
  console.log('✅ Redis连接成功');
});

redisClient.on('error', (err) => {
  console.error('❌ Redis连接错误:', err);
});

// ==================== 数据收集API ====================

/**
 * 收集借阅事件
 */
app.post('/api/collect/borrow', async (req, res) => {
  try {
    const { bookId, userId, timestamp } = req.body;

    if (!bookId || !userId) {
      return res.status(400).json({ error: '缺少必要参数' });
    }

    const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD

    // 更新今日借阅统计
    await redisClient.incr(`stats:today_borrows:${today}`);

    // 更新热门图书排行 (使用有序集合)
    await redisClient.zIncrBy('hot_books', 1, bookId.toString());

    // 记录用户活动
    await redisClient.sAdd('active_users', userId.toString());
    await redisClient.expire('active_users', 3600); // 1小时过期

    // 更新总借阅统计
    await redisClient.incr('stats:total_borrows');

    console.log(`📚 借阅事件记录: 用户${userId} 借阅图书${bookId}`);

    res.json({
      success: true,
      message: '借阅事件记录成功',
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error('记录借阅事件失败:', error);
    res.status(500).json({ error: '服务器内部错误' });
  }
});

/**
 * 收集归还事件
 */
app.post('/api/collect/return', async (req, res) => {
  try {
    const { bookId, userId, timestamp } = req.body;

    if (!bookId || !userId) {
      return res.status(400).json({ error: '缺少必要参数' });
    }

    const today = new Date().toISOString().split('T')[0];

    // 更新今日归还统计
    await redisClient.incr(`stats:today_returns:${today}`);

    // 更新总归还统计
    await redisClient.incr('stats:total_returns');

    console.log(`📖 归还事件记录: 用户${userId} 归还图书${bookId}`);

    res.json({
      success: true,
      message: '归还事件记录成功',
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error('记录归还事件失败:', error);
    res.status(500).json({ error: '服务器内部错误' });
  }
});

/**
 * 收集用户登录事件
 */
app.post('/api/collect/login', async (req, res) => {
  try {
    const { userId, username } = req.body;

    if (!userId) {
      return res.status(400).json({ error: '缺少用户ID' });
    }

    // 添加到在线用户集合
    await redisClient.sAdd('online_users', userId.toString());
    await redisClient.expire('online_users', 3600); // 1小时过期

    // 记录今日登录用户
    const today = new Date().toISOString().split('T')[0];
    await redisClient.sAdd(`stats:daily_login_users:${today}`, userId.toString());
    await redisClient.expire(`stats:daily_login_users:${today}`, 86400 * 7); // 7天过期

    console.log(`👤 用户登录事件: 用户${userId}(${username || 'unknown'}) 上线`);

    res.json({
      success: true,
      message: '用户登录事件记录成功',
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error('记录登录事件失败:', error);
    res.status(500).json({ error: '服务器内部错误' });
  }
});

// ==================== 统计查询API ====================

/**
 * 获取今日统计
 */
app.get('/api/stats/today', async (req, res) => {
  try {
    const today = new Date().toISOString().split('T')[0];

    const [borrows, returns, onlineUsers, dailyLoginUsers] = await Promise.all([
      redisClient.get(`stats:today_borrows:${today}`),
      redisClient.get(`stats:today_returns:${today}`),
      redisClient.sCard('online_users'),
      redisClient.sCard(`stats:daily_login_users:${today}`)
    ]);

    const stats = {
      date: today,
      borrows: parseInt(borrows) || 0,
      returns: parseInt(returns) || 0,
      onlineUsers: onlineUsers || 0,
      dailyLoginUsers: dailyLoginUsers || 0,
      netBorrows: (parseInt(borrows) || 0) - (parseInt(returns) || 0)
    };

    res.json(stats);
  } catch (error) {
    console.error('获取今日统计失败:', error);
    res.status(500).json({ error: '服务器内部错误' });
  }
});

/**
 * 获取热门图书排行
 */
app.get('/api/stats/hot-books', async (req, res) => {
  try {
    const limit = parseInt(req.query.limit) || 10;

    // 获取热门图书数量
    const bookCount = await redisClient.zCard('hot_books');

    if (bookCount === 0) {
      return res.json({
        total: 0,
        books: []
      });
    }

    // 获取所有热门图书成员
    const allBooks = await redisClient.zRange('hot_books', 0, -1);

    // 获取每本书的分数
    const result = [];
    for (let i = 0; i < Math.min(allBooks.length, limit); i++) {
      const bookId = allBooks[allBooks.length - 1 - i]; // 倒序获取
      const score = await redisClient.zScore('hot_books', bookId);
      result.push({
        bookId: bookId,
        borrowCount: score || 0
      });
    }

    res.json({
      total: result.length,
      books: result
    });
  } catch (error) {
    console.error('获取热门图书失败:', error);
    res.status(500).json({ error: '服务器内部错误' });
  }
});

/**
 * 获取总体统计
 */
app.get('/api/stats/overview', async (req, res) => {
  try {
    const [totalBorrows, totalReturns, onlineUsers, hotBooksCount] = await Promise.all([
      redisClient.get('stats:total_borrows'),
      redisClient.get('stats:total_returns'),
      redisClient.sCard('online_users'),
      redisClient.zCard('hot_books')
    ]);

    const stats = {
      totalBorrows: parseInt(totalBorrows) || 0,
      totalReturns: parseInt(totalReturns) || 0,
      currentOnlineUsers: onlineUsers || 0,
      trackedBooks: hotBooksCount || 0,
      systemUptime: process.uptime(),
      lastUpdated: new Date().toISOString()
    };

    res.json(stats);
  } catch (error) {
    console.error('获取总体统计失败:', error);
    res.status(500).json({ error: '服务器内部错误' });
  }
});

/**
 * 获取最近7天统计
 */
app.get('/api/stats/recent-days', async (req, res) => {
  try {
    const days = parseInt(req.query.days) || 7;
    const stats = [];

    for (let i = days - 1; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      const dateStr = date.toISOString().split('T')[0];

      const [borrows, returns, loginUsers] = await Promise.all([
        redisClient.get(`stats:today_borrows:${dateStr}`),
        redisClient.get(`stats:today_returns:${dateStr}`),
        redisClient.sCard(`stats:daily_login_users:${dateStr}`)
      ]);

      stats.push({
        date: dateStr,
        borrows: parseInt(borrows) || 0,
        returns: parseInt(returns) || 0,
        loginUsers: loginUsers || 0
      });
    }

    res.json({
      period: `最近${days}天`,
      data: stats
    });
  } catch (error) {
    console.error('获取最近几天统计失败:', error);
    res.status(500).json({ error: '服务器内部错误' });
  }
});

// ==================== 系统管理API ====================

/**
 * 获取服务状态
 */
app.get('/api/status', async (req, res) => {
  try {
    const redisStatus = redisClient.isOpen ? 'connected' : 'disconnected';

    res.json({
      service: 'Book Analytics Service',
      version: '1.0.0',
      status: 'running',
      redis: redisStatus,
      uptime: process.uptime(),
      memory: process.memoryUsage(),
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    res.status(500).json({ error: '获取状态失败' });
  }
});

/**
 * 清理过期数据
 */
app.post('/api/admin/cleanup', async (req, res) => {
  try {
    // 清理7天前的数据
    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - 7);

    let cleanedCount = 0;

    // 清理过期的每日统计数据
    for (let i = 8; i <= 30; i++) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      const dateStr = date.toISOString().split('T')[0];

      const keys = [
        `stats:today_borrows:${dateStr}`,
        `stats:today_returns:${dateStr}`,
        `stats:daily_login_users:${dateStr}`
      ];

      for (const key of keys) {
        const deleted = await redisClient.del(key);
        cleanedCount += deleted;
      }
    }

    console.log(`🧹 数据清理完成，清理了 ${cleanedCount} 个过期键`);

    res.json({
      success: true,
      message: `数据清理完成，清理了 ${cleanedCount} 个过期键`,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error('数据清理失败:', error);
    res.status(500).json({ error: '数据清理失败' });
  }
});

// ==================== 定时任务 ====================

// 每天凌晨2点清理过期数据
cron.schedule('0 2 * * *', async () => {
  console.log('🕐 开始执行定时数据清理任务...');

  try {
    // 清理30天前的数据
    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - 30);

    let cleanedCount = 0;

    for (let i = 31; i <= 60; i++) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      const dateStr = date.toISOString().split('T')[0];

      const keys = [
        `stats:today_borrows:${dateStr}`,
        `stats:today_returns:${dateStr}`,
        `stats:daily_login_users:${dateStr}`
      ];

      for (const key of keys) {
        const deleted = await redisClient.del(key);
        cleanedCount += deleted;
      }
    }

    console.log(`✅ 定时清理完成，清理了 ${cleanedCount} 个过期键`);
  } catch (error) {
    console.error('❌ 定时清理失败:', error);
  }
});

// 每小时清理离线用户
cron.schedule('0 * * * *', async () => {
  console.log('🕐 清理离线用户...');

  try {
    // 重新设置在线用户过期时间
    const onlineUsers = await redisClient.sMembers('online_users');
    if (onlineUsers.length > 0) {
      await redisClient.expire('online_users', 3600);
      console.log(`✅ 更新了 ${onlineUsers.length} 个在线用户的过期时间`);
    }
  } catch (error) {
    console.error('❌ 清理离线用户失败:', error);
  }
});

// 启动服务器
app.listen(PORT, () => {
  console.log(`🚀 图书分析服务启动成功！`);
  console.log(`📊 服务地址: http://localhost:${PORT}`);
  console.log(`📈 API文档: http://localhost:${PORT}/api/status`);
  console.log(`⏰ 定时任务已启动`);
});

// 优雅关闭
process.on('SIGINT', async () => {
  console.log('\n🛑 正在关闭服务...');

  try {
    await redisClient.quit();
    console.log('✅ Redis连接已关闭');
  } catch (error) {
    console.error('❌ 关闭Redis连接失败:', error);
  }

  process.exit(0);
});
