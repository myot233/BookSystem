package me.myot233.booksystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书实体类
 */
@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 书名
     */
    @Column(nullable = false)
    private String title;
    
    /**
     * 作者
     */
    @Column(nullable = false)
    private String author;
    
    /**
     * 类别
     */
    @Column
    private String category;
    
    /**
     * 出版社
     */
    @Column
    private String publisher;
    
    /**
     * ISBN号
     */
    @Column(unique = true)
    private String isbn;
    
    /**
     * 库存数量
     */
    @Column
    private Integer stock;
    
    /**
     * 已借出数量
     */
    @Column
    private Integer borrowed;
    
    /**
     * 获取可借阅数量
     * @return 可借阅数量
     */
    @Transient
    public Integer getAvailable() {
        return stock - borrowed;
    }
}
