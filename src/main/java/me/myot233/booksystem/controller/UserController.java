package me.myot233.booksystem.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.myot233.booksystem.entity.Book;
import me.myot233.booksystem.entity.User;
import me.myot233.booksystem.service.UserService;

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
     * @param userDetails 当前登录用户
     * @return 用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            Optional<User> user = userService.getUserByUsername(userDetails.getUsername());
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    /**
     * 获取所有用户
     * @return 用户列表
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建用户
     * @param user 用户
     * @return 创建后的用户
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // 检查用户名是否已存在
        if (userService.getUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }
    
    /**
     * 更新用户
     * @param id 用户ID
     * @param user 用户
     * @return 更新后的用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        Optional<User> updatedUser = userService.updateUser(user);
        return updatedUser.map(ResponseEntity::ok)
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
     * 获取用户借阅的图书
     * @param id 用户ID
     * @return 图书列表
     */
    @GetMapping("/{id}/books")
    public ResponseEntity<List<Book>> getUserBooks(@PathVariable Long id) {
        if (userService.getUserById(id).isPresent()) {
            return ResponseEntity.ok(userService.getBorrowedBooks(id));
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 借阅图书
     * @param bookId 图书ID
     * @return 更新后的用户
     */
    @PostMapping("/books/{bookId}")
    public ResponseEntity<User> borrowBook(@PathVariable Long bookId) {
        Optional<User> user = userService.borrowBook(bookId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
    
    /**
     * 归还图书
     * @param bookId 图书ID
     * @return 更新后的用户
     */
    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<User> returnBook(@PathVariable Long bookId) {
        Optional<User> user = userService.returnBook(bookId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}
