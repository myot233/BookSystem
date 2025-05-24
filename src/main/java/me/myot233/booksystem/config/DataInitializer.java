package me.myot233.booksystem.config;

import me.myot233.booksystem.entity.Book;
import me.myot233.booksystem.entity.User;
import me.myot233.booksystem.repository.BookRepository;
import me.myot233.booksystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 数据初始化配置
 */
@Configuration
public class DataInitializer {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 初始化示例数据
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 初始化图书数据
            initBookData();

            // 初始化用户数据
            initUserData();
        };
    }

    /**
     * 初始化图书数据
     */
    private void initBookData() {
        // 检查数据库是否为空
        if (bookRepository.count() == 0) {
            // 添加示例图书数据
            Book book = new Book();
            book.setTitle("三体");
            book.setAuthor("刘慈欣");
            book.setCategory("科幻");
            book.setPublisher("重庆出版社");
            book.setIsbn("9787536692930");
            book.setStock(10);
            book.setBorrowed(3);

            bookRepository.save(book);

            // 可以在这里添加更多示例数据
        }
    }

    /**
     * 初始化用户数据
     */
    private void initUserData() {
        // 检查数据库是否为空
        if (userRepository.count() == 0) {
            // 添加管理员用户
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("管理员");
            admin.setEmail("admin@example.com");
            admin.setRole("ROLE_ADMIN");

            userRepository.save(admin);

            // 添加普通用户
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRealName("普通用户");
            user.setEmail("user@example.com");
            user.setRole("ROLE_USER");

            userRepository.save(user);
        }
    }
}
