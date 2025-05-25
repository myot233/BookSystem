package me.myot233.booksystem.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 自定义JWT认证Token
 * 用于在认证流程中传递JWT字符串，并在认证成功后存储UserDetails和权限。
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String token; // 原始的JWT字符串
    private final Object principal; // 认证成功后是UserDetails，认证前是null

    /**
     * 构造函数：用于认证请求阶段（未认证）。
     * 此时只知道JWT字符串，还未验证其有效性或加载用户权限。
     * @param token 从请求头中提取的JWT字符串
     */
    public JwtAuthenticationToken(String token) {
        super(null); // 未认证时，权限集合为null
        this.token = token;
        this.principal = null; // 未认证时，principal为null
        setAuthenticated(false); // 标记为未认证
    }

    /**
     * 构造函数：用于认证成功后（已认证）。
     * 此时已验证JWT有效，并加载了对应的UserDetails和权限。
     * @param principal 认证成功后的用户主体，通常是UserDetails对象
     * @param token 原始的JWT字符串
     * @param authorities 授予用户的权限集合
     */
    public JwtAuthenticationToken(UserDetails principal, String token,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities); // 已认证时，设置权限集合
        this.principal = principal;
        this.token = token;
        super.setAuthenticated(true); // 标记为已认证
    }

    /**
     * 获取凭据。对于JWT认证，凭据就是JWT字符串本身。
     * @return JWT字符串
     */
    @Override
    public Object getCredentials() {
        return token;
    }

    /**
     * 获取用户主体。认证成功后返回UserDetails对象，认证前返回null。
     * @return 用户主体对象
     */
    @Override
    public Object getPrincipal() {
        return principal;
    }

    /**
     * 获取原始的JWT字符串。
     * @return JWT字符串
     */
    public String getToken() {
        return token;
    }
}