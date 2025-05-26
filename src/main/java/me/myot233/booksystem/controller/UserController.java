package me.myot233.booksystem.controller;

import me.myot233.booksystem.dto.UserDTO;
import me.myot233.booksystem.entity.Book;
import me.myot233.booksystem.entity.User;
import me.myot233.booksystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        // 从SecurityContext中获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getName())) {

            String username = authentication.getName();
            Optional<User> userOpt = userService.getUserByUsername(username);

            if (userOpt.isPresent()) {
                return ResponseEntity.ok(UserDTO.fromUser(userOpt.get()));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * 获取所有用户
     * @return 用户列表
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(UserDTO::fromUser)
                .toList();
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(u -> ResponseEntity.ok(UserDTO.fromUser(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建用户
     * @param user 用户
     * @return 创建后的用户
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody User user) {
        // 检查用户名是否已存在
        if (userService.getUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(UserDTO.fromUser(createdUser), HttpStatus.CREATED);
    }

    /**
     * 更新用户
     * @param id 用户ID
     * @param user 用户
     * @return 更新后的用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        Optional<User> updatedUser = userService.updateUser(user);
        return updatedUser.map(u -> ResponseEntity.ok(UserDTO.fromUser(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.getUserById(id).isPresent()) {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 获取指定用户的借阅图书列表（管理员权限）
     * @param id 用户ID
     * @return 图书列表
     */
    @GetMapping("/{id}/books")
    public ResponseEntity<List<Book>> getUserBooks(@PathVariable Long id) {
        try {
            // 验证用户是否存在
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 获取用户借阅的图书
            List<Book> borrowedBooks = userService.getBorrowedBooks(id);
            return ResponseEntity.ok(borrowedBooks);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 借阅图书（管理员）
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 更新后的用户
     */
    @PostMapping("/{userId}/books/{bookId}")
    public ResponseEntity<UserDTO> borrowBook(@PathVariable Long userId, @PathVariable Long bookId) {
        Optional<User> user = userService.borrowBookForUser(userId, bookId);
        return user.map(u -> ResponseEntity.ok(UserDTO.fromUser(u)))
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * 归还图书（管理员操作）
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 更新后的用户
     */
    @DeleteMapping("/{userId}/books/{bookId}")
    public ResponseEntity<UserDTO> returnBook(@PathVariable Long userId, @PathVariable Long bookId) {
        Optional<User> user = userService.returnBookForUser(userId, bookId);
        return user.map(u -> ResponseEntity.ok(UserDTO.fromUser(u)))
                .orElse(ResponseEntity.badRequest().build());
    }

    // ==================== 用户自己的借阅管理接口 ====================

    /**
     * 获取当前用户的借阅图书列表
     * @return 图书列表
     */
    @GetMapping("/me/books")
    public ResponseEntity<List<Book>> getMyBorrowedBooks() {
        try {
            // 从SecurityContext获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 获取用户名并查找用户
            String username = authentication.getName();
            Optional<User> userOpt = userService.getUserByUsername(username);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 获取用户借阅的图书
            User user = userOpt.get();
            List<Book> borrowedBooks = userService.getBorrowedBooks(user.getId());
            return ResponseEntity.ok(borrowedBooks);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 当前用户借阅图书
     * @param bookId 图书ID
     * @return 更新后的用户
     */
    @PostMapping("/me/books/{bookId}")
    public ResponseEntity<UserDTO> borrowBookForMe(@PathVariable Long bookId) {
        // 从SecurityContext中获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getName())) {

            Optional<User> updatedUser = userService.borrowBook(bookId);
            return updatedUser.map(user -> ResponseEntity.ok(UserDTO.fromUser(user)))
                    .orElse(ResponseEntity.badRequest().build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * 当前用户归还图书
     * @param bookId 图书ID
     * @return 更新后的用户
     */
    @DeleteMapping("/me/books/{bookId}")
    public ResponseEntity<UserDTO> returnBookForMe(@PathVariable Long bookId) {
        // 从SecurityContext中获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getName())) {

            Optional<User> updatedUser = userService.returnBook(bookId);
            return updatedUser.map(user -> ResponseEntity.ok(UserDTO.fromUser(user)))
                    .orElse(ResponseEntity.badRequest().build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
