package me.myot233.booksystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.myot233.booksystem.service.UserService;
import me.myot233.booksystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // 跳过不需要JWT认证的路径
        if (shouldSkipFilter(requestPath)) {
            logger.info("跳过JWT过滤器，路径: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token格式为 "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
                logger.info("从JWT Token中获取到用户名: " + username);
            } catch (Exception e) {
                logger.warn("无法获取JWT Token中的用户名: " + e.getMessage());
            }
        } else {
            logger.warn("请求头中没有有效的Authorization Bearer token: " + requestTokenHeader);
        }

        // 验证token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userService.loadUserByUsername(username);
                logger.info("加载用户详情成功: " + username + ", 角色: " + userDetails.getAuthorities());

                // 如果token有效，设置Spring Security的认证
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("认证成功，用户: " + username);
                } else {
                    logger.warn("JWT Token验证失败: " + username);
                }
            } catch (Exception e) {
                logger.error("加载用户详情失败: " + username + ", 错误: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否应该跳过JWT过滤器
     * @param requestPath 请求路径
     * @return 是否跳过
     */
    private boolean shouldSkipFilter(String requestPath) {
        // 允许的路径列表
        String[] allowedPaths = {
            "/",
            "/health",
            "/api/auth/login",
            "/api/auth/register"
        };

        // 检查精确匹配
        for (String path : allowedPaths) {
            if (requestPath.equals(path)) {
                return true;
            }
        }

        // 检查前缀匹配
        String[] allowedPrefixes = {
            "/api/auth/",
            "/ws/"
        };

        for (String prefix : allowedPrefixes) {
            if (requestPath.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }
}
