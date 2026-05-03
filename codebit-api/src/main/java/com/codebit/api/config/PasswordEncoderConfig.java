package com.codebit.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean // 2. 告诉 Spring：调用这个方法，把返回的对象注册到容器中
    public PasswordEncoder passwordEncoder() {
        // BCrypt 是一种自适应哈希函数，可以配置计算强度
        // 特点：
        // 1. 自动生成随机盐值（每次加密结果不同）
        // 2. 可配置计算强度（参数 4-31，默认 10），强度越高越安全但耗时越长
        // 3. 抗彩虹表攻击
        return new BCryptPasswordEncoder();
    }
}