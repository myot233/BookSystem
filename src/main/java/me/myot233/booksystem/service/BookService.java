package me.myot233.booksystem.service;

import me.myot233.booksystem.entity.Book;
import me.myot233.booksystem.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 图书服务类
 */
@Service
public class BookService {
    
    private final BookRepository bookRepository;
    
    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    /**
     * 获取所有图书
     * @return 图书列表
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    /**
     * 根据ID获取图书
     * @param id 图书ID
     * @return 图书
     */
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }
    
    /**
     * 根据ISBN获取图书
     * @param isbn ISBN号
     * @return 图书
     */
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
    
    /**
     * 根据书名查找图书
     * @param title 书名
     * @return 图书列表
     */
    public List<Book> getBooksByTitle(String title) {
        return bookRepository.findByTitleContaining(title);
    }
    
    /**
     * 根据作者查找图书
     * @param author 作者
     * @return 图书列表
     */
    public List<Book> getBooksByAuthor(String author) {
        return bookRepository.findByAuthorContaining(author);
    }
    
    /**
     * 根据类别查找图书
     * @param category 类别
     * @return 图书列表
     */
    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }
    
    /**
     * 保存图书
     * @param book 图书
     * @return 保存后的图书
     */
    @Transactional
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }
    
    /**
     * 删除图书
     * @param id 图书ID
     */
    @Transactional
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
    
    /**
     * 借阅图书
     * @param id 图书ID
     * @return 借阅后的图书
     */
    @Transactional
    public Optional<Book> borrowBook(Long id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (book.getAvailable() > 0) {
                book.setBorrowed(book.getBorrowed() + 1);
                return Optional.of(bookRepository.save(book));
            }
        }
        return Optional.empty();
    }
    
    /**
     * 归还图书
     * @param id 图书ID
     * @return 归还后的图书
     */
    @Transactional
    public Optional<Book> returnBook(Long id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (book.getBorrowed() > 0) {
                book.setBorrowed(book.getBorrowed() - 1);
                return Optional.of(bookRepository.save(book));
            }
        }
        return Optional.empty();
    }
}
