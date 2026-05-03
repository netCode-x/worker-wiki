package com.codebit.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/2 星期四
 * @Description:
 * @VERSON: 17
 */


@SpringBootApplication
@EnableJpaAuditing  // 开启审计功能，使 @CreatedDate 和 @LastModifiedDate 生效
@EnableAsync
public class CodeBitApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeBitApplication.class ,args);
    }
}
