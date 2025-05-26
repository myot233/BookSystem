package me.myot233.booksystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 密码
     */
    @Column(nullable = false)
    private String password;

    /**
     * 真实姓名
     */
    @Column
    private String realName;

    /**
     * 电子邮件
     */
    @Column
    private String email;

    /**
     * 手机号码
     */
    @Column
    private String phone;

    /**
     * 用户角色
     */
    @Column(nullable = false)
    private String role = "ROLE_USER"; // 默认为普通用户

    /**
     * 账户是否未过期
     */
    @Column(nullable = false)
    private boolean accountNonExpired = true;

    /**
     * 账户是否未锁定
     */
    @Column(nullable = false)
    private boolean accountNonLocked = true;

    /**
     * 凭证是否未过期
     */
    @Column(nullable = false)
    private boolean credentialsNonExpired = true;

    /**
     * 账户是否启用
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime = new Date();

    /**
     * 最后登录时间
     */
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginTime;

    /**
     * 借阅的图书
     */
    @JsonIgnore  // 避免JSON序列化时的懒加载问题
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_book_borrowings",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<Book> borrowedBooks = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
