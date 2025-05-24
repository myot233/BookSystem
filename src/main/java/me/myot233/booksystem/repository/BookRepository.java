package me.myot233.booksystem.repository;

import me.myot233.booksystem.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 图书数据访问接口
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    /**
     * 根据书名查找图书
     * @param title 书名
     * @return 图书列表
     */
    List<Book> findByTitleContaining(String title);
    
    /**
     * 根据作者查找图书
     * @param author 作者
     * @return 图书列表
     */
    List<Book> findByAuthorContaining(String author);
    
    /**
     * 根据类别查找图书
     * @param category 类别
     * @return 图书列表
     */
    List<Book> findByCategory(String category);
    
    /**
     * 根据ISBN查找图书
     * @param isbn ISBN号
     * @return 图书
     */
    Optional<Book> findByIsbn(String isbn);
    
    /**
     * 根据书名和作者查找图书
     * @param title 书名
     * @param author 作者
     * @return 图书列表
     */
    List<Book> findByTitleContainingAndAuthorContaining(String title, String author);
}
