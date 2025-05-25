package me.myot233.booksystem.service;

import me.myot233.booksystem.entity.Book;
import me.myot233.booksystem.repository.BookRepository;
import me.myot233.booksystem.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 图书服务类
 */
@Service
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Redis键前缀
    private static final String BOOK_CACHE_PREFIX = "book:";
    private static final String BOOK_LIST_CACHE_PREFIX = "book_list:";
    private static final String BOOK_SEARCH_CACHE_PREFIX = "book_search:";
    private static final String BOOK_STATS_PREFIX = "book_stats:";
    private static final String HOT_BOOKS_KEY = "hot_books";

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 获取所有图书（带缓存）
     * @return 图书列表
     */
    @Cacheable(value = "books", key = "'all'")
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * 根据ID获取图书（带缓存）
     * @param id 图书ID
     * @return 图书
     */
    @Cacheable(value = "books", key = "#id")
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    /**
     * 根据ISBN获取图书（带缓存）
     * @param isbn ISBN号
     * @return 图书
     */
    @Cacheable(value = "books", key = "'isbn:' + #isbn")
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    /**
     * 根据书名查找图书（带缓存）
     * @param title 书名
     * @return 图书列表
     */
    @Cacheable(value = "book_search", key = "'title:' + #title")
    public List<Book> getBooksByTitle(String title) {
        return bookRepository.findByTitleContaining(title);
    }

    /**
     * 根据作者查找图书（带缓存）
     * @param author 作者
     * @return 图书列表
     */
    @Cacheable(value = "book_search", key = "'author:' + #author")
    public List<Book> getBooksByAuthor(String author) {
        return bookRepository.findByAuthorContaining(author);
    }

    /**
     * 根据类别查找图书（带缓存）
     * @param category 类别
     * @return 图书列表
     */
    @Cacheable(value = "book_search", key = "'category:' + #category")
    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    /**
     * 保存图书（更新缓存）
     * @param book 图书
     * @return 保存后的图书
     */
    @Transactional
    @CachePut(value = "books", key = "#result.id")
    @CacheEvict(value = {"books", "book_search"}, allEntries = true)
    public Book saveBook(Book book) {
        Book savedBook = bookRepository.save(book);
        // 清除相关缓存
        clearBookCaches();
        return savedBook;
    }

    /**
     * 删除图书（清除缓存）
     * @param id 图书ID
     */
    @Transactional
    @CacheEvict(value = {"books", "book_search"}, allEntries = true)
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
        // 清除相关缓存
        clearBookCaches();
        redisUtil.del(BOOK_CACHE_PREFIX + id);
    }

    /**
     * 借阅图书（带分布式锁和缓存更新）
     * @param id 图书ID
     * @return 借阅后的图书
     */
    @Transactional
    @CacheEvict(value = {"books", "book_search"}, key = "#id")
    public Optional<Book> borrowBook(Long id) {
        String lockKey = "book_borrow_lock:" + id;
        try {
            // 使用Redis分布式锁防止超借
            if (redisUtil.set(lockKey, "locked", 10)) {
                Optional<Book> bookOpt = bookRepository.findById(id);
                if (bookOpt.isPresent()) {
                    Book book = bookOpt.get();
                    if (book.getAvailable() > 0) {
                        book.setBorrowed(book.getBorrowed() + 1);
                        Book savedBook = bookRepository.save(book);

                        // 更新热门图书统计
                        updateBookPopularity(id);

                        // 更新缓存
                        redisUtil.set(BOOK_CACHE_PREFIX + id, savedBook, 3600);

                        return Optional.of(savedBook);
                    }
                }
            }
        } finally {
            // 释放锁
            redisUtil.del(lockKey);
        }
        return Optional.empty();
    }

    /**
     * 归还图书（带缓存更新）
     * @param id 图书ID
     * @return 归还后的图书
     */
    @Transactional
    @CacheEvict(value = {"books", "book_search"}, key = "#id")
    public Optional<Book> returnBook(Long id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (book.getBorrowed() > 0) {
                book.setBorrowed(book.getBorrowed() - 1);
                Book savedBook = bookRepository.save(book);

                // 更新缓存
                redisUtil.set(BOOK_CACHE_PREFIX + id, savedBook, 3600);

                return Optional.of(savedBook);
            }
        }
        return Optional.empty();
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
        List<Book> hotBooks = new java.util.ArrayList<>();

        if (hotBookIds != null) {
            for (Object bookId : hotBookIds) {
                Optional<Book> book = getBookById(Long.valueOf(bookId.toString()));
                book.ifPresent(hotBooks::add);
            }
        }

        // 缓存结果30分钟
        redisUtil.set(cacheKey, hotBooks, 1800);
        return hotBooks;
    }

    /**
     * 更新图书热度
     * @param bookId 图书ID
     */
    private void updateBookPopularity(Long bookId) {
        // 增加图书的借阅次数统计
        redisTemplate.opsForZSet().incrementScore(HOT_BOOKS_KEY, bookId.toString(), 1);

        // 记录今日借阅统计
        String todayKey = BOOK_STATS_PREFIX + "today:" + java.time.LocalDate.now();
        redisUtil.incr(todayKey + ":" + bookId, 1);
        redisUtil.expire(todayKey + ":" + bookId, 86400 * 7); // 保存7天
    }

    /**
     * 获取图书借阅统计
     * @param bookId 图书ID
     * @return 借阅次数
     */
    public Long getBookBorrowCount(Long bookId) {
        Double score = redisTemplate.opsForZSet().score(HOT_BOOKS_KEY, bookId.toString());
        return score != null ? score.longValue() : 0L;
    }

    /**
     * 清除图书相关缓存
     */
    private void clearBookCaches() {
        // 清除热门图书缓存
        Set<String> keys = redisTemplate.keys(HOT_BOOKS_KEY + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        // 清除搜索缓存
        Set<String> searchKeys = redisTemplate.keys(BOOK_SEARCH_CACHE_PREFIX + "*");
        if (searchKeys != null && !searchKeys.isEmpty()) {
            redisTemplate.delete(searchKeys);
        }
    }

    /**
     * 获取今日借阅统计
     * @return 今日借阅总数
     */
    public Long getTodayBorrowCount() {
        String todayKey = BOOK_STATS_PREFIX + "today_total:" + java.time.LocalDate.now();
        Object count = redisUtil.get(todayKey);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    /**
     * 增加今日借阅统计
     */
    public void incrementTodayBorrowCount() {
        String todayKey = BOOK_STATS_PREFIX + "today_total:" + java.time.LocalDate.now();
        redisUtil.incr(todayKey, 1);
        redisUtil.expire(todayKey, 86400); // 24小时过期
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
     * 添加在线用户
     * @param userId 用户ID
     */
    public void addOnlineUser(Long userId) {
        String onlineUsersKey = "online_users";
        redisUtil.sSet(onlineUsersKey, userId.toString());
        redisUtil.expire(onlineUsersKey, 1800); // 30分钟过期
    }

    /**
     * 移除在线用户
     * @param userId 用户ID
     */
    public void removeOnlineUser(Long userId) {
        String onlineUsersKey = "online_users";
        redisUtil.setRemove(onlineUsersKey, userId.toString());
    }
}
