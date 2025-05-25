package me.myot233.booksystem.config;

import me.myot233.booksystem.security.JwtAuthenticationFilter;
import me.myot233.booksystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * 安全配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(UserService userService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userService = userService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 密码编码器
     * @return BCrypt密码编码器
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证提供者
     * @return DAO认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 认证管理器
     * @param authConfig 认证配置
     * @return 认证管理器
     * @throws Exception 异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 安全过滤链
     * @param http HTTP安全
     * @return 安全过滤链
     * @throws Exception 异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // 禁用CSRF保护
            .authorizeHttpRequests(authorize -> authorize
                // 允许系统信息和健康检查接口
                .requestMatchers("/", "/health").permitAll()
                // 允许认证相关请求（包括登录和注册）
                .requestMatchers("/api/auth/**").permitAll()
                // 允许WebSocket连接
                .requestMatchers("/ws/**").permitAll()
                // 允许图书查询请求
                .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                // 图书管理操作需要管理员权限
                .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
                // 当前用户信息需要认证（必须在/api/users/**之前）
                .requestMatchers("/api/users/me").authenticated()
                // 用户自己的借阅管理需要认证（普通用户权限）
                .requestMatchers("/api/users/me/books/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/me/books").authenticated()
                // 管理员管理所有用户借阅需要管理员权限
                .requestMatchers("/api/users/*/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/*/books").hasRole("ADMIN")
                // 用户管理需要管理员权限
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                // 通知相关接口需要认证
                .requestMatchers("/api/notifications/**").authenticated()
                // /admin/**路径，需要ADMIN角色
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // /user/**路径，需要USER或ADMIN角色
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                // 任何其他未匹配的请求，需要认证
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 会话管理策略为无状态
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 添加JWT过滤器

        return http.build();
    }
}
