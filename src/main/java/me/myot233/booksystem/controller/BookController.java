package me.myot233.booksystem.controller;

import me.myot233.booksystem.entity.Book;
import me.myot233.booksystem.service.BookService;
import me.myot233.booksystem.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 图书控制器
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 获取所有图书
     *
     * @return 图书列表
     */
    @GetMapping("/")
    public Response<List<Book>> getAllBooks() {
        return Response.ok(bookService.getAllBooks());
    }

    /**
     * 根据ID获取图书
     *
     * @param id 图书ID
     * @return 图书
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);
        return book.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据ISBN获取图书
     *
     * @param isbn ISBN号
     * @return 图书
     */
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        Optional<Book> book = bookService.getBookByIsbn(isbn);
        return book.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据书名查找图书
     *
     * @param title 书名
     * @return 图书列表
     */
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> getBooksByTitle(@RequestParam String title) {
        return ResponseEntity.ok(bookService.getBooksByTitle(title));
    }

    /**
     * 根据作者查找图书
     *
     * @param author 作者
     * @return 图书列表
     */
    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> getBooksByAuthor(@RequestParam String author) {
        return ResponseEntity.ok(bookService.getBooksByAuthor(author));
    }

    /**
     * 根据类别查找图书
     *
     * @param category 类别
     * @return 图书列表
     */
    @GetMapping("/search/category")
    public ResponseEntity<List<Book>> getBooksByCategory(@RequestParam String category) {
        return ResponseEntity.ok(bookService.getBooksByCategory(category));
    }

    /**
     * 添加图书
     *
     * @param book 图书
     * @return 添加后的图书
     */
    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        return new ResponseEntity<>(bookService.saveBook(book), HttpStatus.CREATED);
    }

    /**
     * 更新图书
     *
     * @param id   图书ID
     * @param book 图书
     * @return 更新后的图书
     */
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        Optional<Book> existingBook = bookService.getBookById(id);
        if (existingBook.isPresent()) {
            book.setId(id);
            return ResponseEntity.ok(bookService.saveBook(book));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除图书
     *
     * @param id 图书ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        Optional<Book> existingBook = bookService.getBookById(id);
        if (existingBook.isPresent()) {
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 借阅图书
     *
     * @param id 图书ID
     * @return 借阅后的图书
     */
    @PutMapping("/{id}/borrow")
    public ResponseEntity<Book> borrowBook(@PathVariable Long id) {
        Optional<Book> book = bookService.borrowBook(id);
        return book.map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * 归还图书
     *
     * @param id 图书ID
     * @return 归还后的图书
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<Book> returnBook(@PathVariable Long id) {
        Optional<Book> book = bookService.returnBook(id);
        return book.map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}
