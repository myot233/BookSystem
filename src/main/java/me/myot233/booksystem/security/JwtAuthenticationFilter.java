package me.myot233.booksystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器。
 * 负责从请求头中提取JWT Token，创建JwtAuthenticationToken，并将其提交给AuthenticationManager进行认证。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    // 构造函数注入AuthenticationManager
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        String jwtToken = null;

        // JWT Token格式为 "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
        }

        // 如果存在JWT Token，并且当前SecurityContext中没有认证信息
        if (StringUtils.hasText(jwtToken) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 1. 创建一个未认证的JwtAuthenticationToken
                JwtAuthenticationToken authenticationRequest = new JwtAuthenticationToken(jwtToken);

                // 2. 将认证请求提交给AuthenticationManager
                // AuthenticationManager会找到JwtAuthenticationProvider来处理这个请求
                Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);

                // 3. 认证成功，将已认证的Authentication对象设置到SecurityContext中
                SecurityContextHolder.getContext().setAuthentication(authenticationResult);

            } catch (AuthenticationException e) {
                // 认证失败，清除SecurityContext，并发送401 Unauthorized响应
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Authentication Failed: " + e.getMessage());
                return; // 认证失败，不再继续过滤器链
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}