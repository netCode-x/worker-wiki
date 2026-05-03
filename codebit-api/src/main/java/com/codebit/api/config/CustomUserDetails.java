package com.codebit.api.config;

import com.codebit.api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    // 适配器模式：将我们的 User 实体包装成 Spring Security 需要的 UserDetails 对象

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回用户的权限/角色列表
        // 这里返回空集合，表示没有细粒度权限控制
        // 如果有权限需求，可以从 user 中获取角色列表并转换为 GrantedAuthority
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();  // 返回加密后的密码
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 账户是否未过期（true 表示未过期）
        // 可扩展：从 user 中读取过期时间字段
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 账户是否未锁定（true 表示未锁定）
        // 可扩展：从 user 中读取锁定状态字段
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 凭证（密码）是否未过期
        // 可扩展：实现密码过期强制修改功能
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 账户是否启用
        // 可扩展：实现账号禁用/启用功能
        return true;
    }

    // 额外方法：获取用户ID（方便业务使用）
    public Long getUserId() {
        return Long.valueOf(user.getId());
    }
}