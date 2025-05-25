package me.myot233.booksystem.config;

import me.myot233.booksystem.security.JwtAuthenticationFilter;
import me.myot233.booksystem.security.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

/**
 * 安全配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationProvider authenticationProvider;

    @Autowired
    public SecurityConfig( JwtAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
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
     * 安全过滤链
     * @param http HTTP安全
     * @return 安全过滤链
     * @throws Exception 异常
     */
    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // 禁用CSRF保护
            .authorizeHttpRequests(authorize -> authorize
                // 允许系统信息和健康检查接口
                .requestMatchers("/", "/health").permitAll()
                // 允许认证相关请求
                .requestMatchers("/api/auth/**").permitAll()
                // 允许WebSocket连接
                .requestMatchers("/ws/**").permitAll()
                // 允许图书相关请求（临时开放）
                .requestMatchers("/api/books/**").hasAnyRole("USER", "ADMIN")
                // 允许用户注册
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                // 当前用户信息需要认证
                .requestMatchers("/api/users/me").authenticated()
                // 通知相关接口需要认证
                .requestMatchers("/api/notifications/**").authenticated()
                // 用户管理需要管理员权限
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                // /admin/**路径，需要ADMIN角色
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // /user/**路径，需要USER或ADMIN角色
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                // 任何其他未匹配的请求，需要认证
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 会话管理策略为无状态
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        // 使用ProviderManager来管理自定义的AuthenticationProvider
        // Collections.singletonList确保只包含我们的JwtAuthenticationProvider
        return new ProviderManager(Collections.singletonList(authenticationProvider));
    }

    /**
     * 创建JwtAuthenticationFilter的Bean。
     * 需要注入AuthenticationManager，以便过滤器可以将认证请求委托给它。
     * @param authenticationManager 认证管理器
     * @return JwtAuthenticationFilter实例
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new JwtAuthenticationFilter(authenticationManager);
    }


}
