package com.codebit.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.function.Predicate;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/2 星期四
 * @Description:
 * @VERSON: 17
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    // 实现 WebMvcConfigurer 接口，用于自定义 Spring MVC 的配置（如跨域、拦截器、格式化器等）


    /**
     *  统一访问路径配置
     * @param configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
              configurer.addPathPrefix("api", new Predicate<Class<?>>() {
                  @Override
                  public boolean test(Class<?> aClass) {
                      return true;
                  }
              });
    }

    /**
     * 重写 addCorsMappings 方法，该方法用于配置跨域资源共享（CORS）规则
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowCredentials(false)
                .maxAge(3600)
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
