package me.myot233.booksystem.repository;

import me.myot233.booksystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据电子邮件查找用户
     * @param email 电子邮件
     * @return 用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号码查找用户
     * @param phone 手机号码
     * @return 用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查电子邮件是否存在
     * @param email 电子邮件
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号码是否存在
     * @param phone 手机号码
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
}
