package me.myot233.booksystem.security;

import me.myot233.booksystem.service.UserService; // 假设你的UserService实现了UserDetailsService
import me.myot233.booksystem.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * 自定义JWT认证提供者。
 * 负责验证JwtAuthenticationToken，并根据JWT内容加载用户详情和权限。
 */
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtUtil jwtUtil;
    private final UserService userService; // 假设UserService实现了UserDetailsService

    public JwtAuthenticationProvider(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    /**
     * 执行认证逻辑。
     * 当AuthenticationManager接收到JwtAuthenticationToken时，会调用此方法。
     * @param authentication 未认证的Authentication对象，此处应为JwtAuthenticationToken
     * @return 认证成功后，返回一个已认证的Authentication对象
     * @throws AuthenticationException 如果认证失败，抛出此异常
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 1. 确保传入的Authentication是JwtAuthenticationToken类型
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return null; // 如果不是此Provider支持的类型，则返回null，让其他Provider处理
        }

        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        String jwtToken = (String) jwtAuthenticationToken.getCredentials(); // 获取JWT字符串

        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new BadCredentialsException("JWT Token is missing.");
        }

        String username;
        try {
            // 2. 从JWT中提取用户名
            username = jwtUtil.getUsernameFromToken(jwtToken);
        } catch (Exception e) {
            // JWT解析失败，例如Token过期、签名无效等
            throw new BadCredentialsException("Invalid JWT Token: " + e.getMessage(), e);
        }

        if (username == null) {
            throw new BadCredentialsException("Username could not be extracted from JWT Token.");
        }

        // 3. 加载用户详细信息
        UserDetails userDetails;
        try {
            userDetails = userService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("User not found for the given JWT Token.", e);
        }

        // 4. 验证JWT Token的有效性（包括过期时间、签名等）
        if (!jwtUtil.validateToken(jwtToken, userDetails)) {
            throw new BadCredentialsException("JWT Token is invalid or expired.");
        }

        // 5. 认证成功，返回一个已认证的JwtAuthenticationToken
        // 此时principal是UserDetails，并且权限集合也已设置
        return new JwtAuthenticationToken(userDetails, jwtToken, userDetails.getAuthorities());
    }

    /**
     * 判断此Provider是否支持给定的Authentication类型。
     * @param authentication 需要认证的Authentication类型
     * @return 如果支持JwtAuthenticationToken，则返回true
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}