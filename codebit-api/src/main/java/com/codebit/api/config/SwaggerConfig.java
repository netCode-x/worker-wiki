package com.codebit.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/2 星期四
 * @Description:
 * @VERSON: 17
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("wiki个人博客 服务 API 文档")
                        .description("基于 springdoc-openapi 的wiki 个人博客服务")
                        .version("1.0")
                        .contact(new Contact()
                                .name("yangkaihu")
                                .email("yangkaihu@yeah.net")
                                .url("http://localhost:8888/swagger-ui/index.html"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }

}
