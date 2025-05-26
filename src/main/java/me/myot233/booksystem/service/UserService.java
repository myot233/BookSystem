package me.myot233.booksystem.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.myot233.booksystem.entity.Book;
import me.myot233.booksystem.entity.User;
import me.myot233.booksystem.repository.BookRepository;
import me.myot233.booksystem.repository.UserRepository;

/**
 * 用户服务类
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnalyticsService analyticsService;
    private final BookService bookService;
    private final StatisticsService statisticsService;

    @Autowired
    public UserService(UserRepository userRepository, BookRepository bookRepository,
                      PasswordEncoder passwordEncoder, AnalyticsService analyticsService,
                      BookService bookService, StatisticsService statisticsService) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
        this.analyticsService = analyticsService;
        this.bookService = bookService;
        this.statisticsService = statisticsService;
    }

    /**
     * 根据用户名加载用户
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户名未找到异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户名不存在: " + username));
    }

    /**
     * 获取所有用户
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 创建用户
     * @param user 用户
     * @return 创建后的用户
     */
    @Transactional
    public User createUser(User user) {
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword() == null ? "abc123" : user.getPassword() ));
        // 设置创建时间
        user.setCreateTime(new Date());
        return userRepository.save(user);
    }

    /**
     * 更新用户
     * @param user 用户
     * @return 更新后的用户
     */
    @Transactional
    public Optional<User> updateUser(User user) {
        return userRepository.findById(user.getId())
                .map(existingUser -> {
                    // 如果密码已更改，则加密新密码
                    if (!existingUser.getPassword().equals(user.getPassword())) {
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                    return userRepository.save(user);
                });
    }

    /**
     * 删除用户
     * @param id 用户ID
     */
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * 借阅图书
     * @param bookId 图书ID
     * @return 更新后的用户
     */
    @Transactional
    public Optional<User> borrowBook(Long bookId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 使用BookService处理图书借阅逻辑，避免并发冲突
            Optional<Book> borrowedBookOpt = bookService.borrowBook(bookId);

            if (borrowedBookOpt.isPresent()) {
                Book book = borrowedBookOpt.get();

                // 更新用户借阅信息
                user.getBorrowedBooks().add(book);
                User savedUser = userRepository.save(user);

                // 发送借阅事件到分析服务
                analyticsService.sendBorrowEvent(bookId, user.getId());

                // 更新用户活跃度
                statisticsService.updateUserActivity(user.getId());

                return Optional.of(savedUser);
            }
        }

        return Optional.empty();
    }

    /**
     * 归还图书
     * @param bookId 图书ID
     * @return 更新后的用户
     */
    @Transactional
    public Optional<User> returnBook(Long bookId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 检查用户是否借阅了该图书
            if (user.getBorrowedBooks().removeIf(b -> b.getId().equals(bookId))) {
                // 使用BookService处理图书归还逻辑，避免并发冲突
                bookService.returnBook(bookId);

                User savedUser = userRepository.save(user);

                // 发送归还事件到分析服务
                analyticsService.sendReturnEvent(bookId, user.getId());

                // 更新用户活跃度
                statisticsService.updateUserActivity(user.getId());

                return Optional.of(savedUser);
            }
        }

        return Optional.empty();
    }

    /**
     * 管理员帮指定用户借阅图书
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 更新后的用户
     */
    @Transactional
    public Optional<User> borrowBookForUser(Long userId, Long bookId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 使用BookService处理图书借阅逻辑，避免并发冲突
            Optional<Book> borrowedBookOpt = bookService.borrowBook(bookId);

            if (borrowedBookOpt.isPresent()) {
                Book book = borrowedBookOpt.get();

                // 更新用户借阅信息
                user.getBorrowedBooks().add(book);
                User savedUser = userRepository.save(user);

                // 发送借阅事件到分析服务
                analyticsService.sendBorrowEvent(bookId, user.getId());

                // 更新用户活跃度
                statisticsService.updateUserActivity(user.getId());

                return Optional.of(savedUser);
            }
        }

        return Optional.empty();
    }

    /**
     * 管理员帮指定用户归还图书
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 更新后的用户
     */
    @Transactional
    public Optional<User> returnBookForUser(Long userId, Long bookId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 检查用户是否借阅了该图书
            if (user.getBorrowedBooks().removeIf(b -> b.getId().equals(bookId))) {
                // 使用BookService处理图书归还逻辑，避免并发冲突
                bookService.returnBook(bookId);

                User savedUser = userRepository.save(user);

                // 发送归还事件到分析服务
                analyticsService.sendReturnEvent(bookId, user.getId());

                // 更新用户活跃度
                statisticsService.updateUserActivity(user.getId());

                return Optional.of(savedUser);
            }
        }

        return Optional.empty();
    }

    /**
     * 获取用户借阅的图书
     * @param userId 用户ID
     * @return 图书列表
     */
    @Transactional(readOnly = true)
    public List<Book> getBorrowedBooks(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // 强制初始化懒加载集合
                    List<Book> borrowedBooks = user.getBorrowedBooks();
                    // 触发懒加载，确保数据在事务内被加载
                    borrowedBooks.size();
                    return new ArrayList<>(borrowedBooks);
                })
                .orElse(new ArrayList<>());
    }

    /**
     * 更新最后登录时间
     * @param username 用户名
     */
    @Transactional
    public void updateLastLoginTime(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    user.setLastLoginTime(new Date());
                    userRepository.save(user);
                });
    }
}
