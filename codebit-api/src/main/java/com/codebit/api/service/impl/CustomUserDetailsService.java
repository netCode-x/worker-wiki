package com.codebit.api.service.impl;

import com.codebit.api.config.CustomUserDetails;
import com.codebit.api.entity.User;
import com.codebit.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    // UserDetailsService 是 Spring Security 的核心接口
    // 用于根据用户名加载用户信息

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户
        User user = userRepository.findByUsername(username)
                // 如果不存在，抛出 UsernameNotFoundException（Spring Security 标准异常）
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 将我们的 User 实体包装成 Spring Security 认可的 UserDetails 对象
        return new CustomUserDetails(user);
    }
}