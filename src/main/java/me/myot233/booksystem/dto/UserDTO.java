package me.myot233.booksystem.dto;

import me.myot233.booksystem.entity.User;
import lombok.Data;

import java.util.Date;

/**
 * 用户数据传输对象
 * 避免直接返回User实体导致的懒加载问题
 */
@Data
public class UserDTO {
    
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String role;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private Date createTime;
    private Date lastLoginTime;
    
    /**
     * 从User实体创建UserDTO
     * @param user 用户实体
     * @return UserDTO
     */
    public static UserDTO fromUser(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setAccountNonExpired(user.isAccountNonExpired());
        dto.setAccountNonLocked(user.isAccountNonLocked());
        dto.setCredentialsNonExpired(user.isCredentialsNonExpired());
        dto.setEnabled(user.isEnabled());
        dto.setCreateTime(user.getCreateTime());
        dto.setLastLoginTime(user.getLastLoginTime());
        
        return dto;
    }
}
