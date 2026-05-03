package com.codebit.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity   // 启用 Spring Security 的 Web 安全支持，自动配置安全过滤链
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器，实现依赖注入（比 @Autowired 更推荐）
public class SecurityConfig {
    // 通过构造器注入 JWT 认证过滤器
    // JwtAuthenticationFilter 负责拦截请求、解析 Token、设置认证上下文
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    /**
     * 配置安全过滤链（Spring Security 的核心配置）
     * @param http HttpSecurity 配置构建器
     * @return SecurityFilterChain 安全过滤链
     */
    @Bean // 将该方法的返回值注册为 Spring 容器中的 Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ==================== 1. CSRF 防护配置 ====================
                // .csrf(AbstractHttpConfigurer::disable) 的作用：
                // - 禁用跨站请求伪造（CSRF）防护
                // - 原因：我们使用 JWT 进行无状态认证，服务器不存储 Session
                // - CSRF 防护主要针对基于 Session 的认证方式，JWT 场景下不需要
                // - 禁用后可以减少请求处理开销，简化配置
                .csrf(AbstractHttpConfigurer::disable)

                // ==================== 2. Session 管理配置 ====================
               // .sessionManagement(session -> session
                        // .sessionCreationPolicy(SessionCreationPolicy.STATELESS) 的作用：
                        // - 设置 Session 创建策略为"无状态"
                        // - NEVER: Spring Security 不会主动创建 Session，但如果已有 Session 仍会使用
                        // - STATELESS: 严格无状态，Spring Security 永远不会创建 Session，也从不使用 Session
                        // - 原因：JWT 认证是无状态的，每个请求都独立携带 Token
                        // - 好处：不需要共享 Session 存储，便于水平扩展（任意节点都能处理请求）
                     //   .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // .requestMatchers(...).permitAll() 的作用：
                        // - 配置哪些 URL 路径允许公开访问（无需认证）
                        // - 这些路径不需要携带 Token 就能访问
                        // - 原因：注册、登录接口必须公开，否则用户无法登录
                        //        Swagger 文档也需要公开，方便前端查看 API
                        .requestMatchers(
                                "/api/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // .anyRequest().authenticated() 的作用：
                        // - 所有未在上面匹配到的请求都需要认证
                        // - 如果请求没有携带有效 Token，会返回 401 Unauthorized
                        // - 原因：保护需要登录才能访问的资源
                        .anyRequest().authenticated()
                )
                // ==================== 4. 添加自定义过滤器 ====================
                // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) 的作用：
                // - 将 JWT 认证过滤器添加到 Spring Security 的过滤器链中
                // - 指定在 UsernamePasswordAuthenticationFilter 之前执行
                // - 原因：
                //   1. JWT 过滤器需要先解析 Token，如果解析成功则设置认证信息
                //   2. 设置完成后，后续的过滤器可以直接使用已认证的信息
                //   3. 放在 UsernamePasswordAuthenticationFilter 之前，避免该过滤器尝试表单登录
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 提供 AuthenticationManager Bean
     * @param config 认证配置
     * @return AuthenticationManager 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // config.getAuthenticationManager() 的作用：
        // - 获取 Spring Security 默认的 AuthenticationManager
        // - 该管理器负责处理认证请求（如用户名密码认证）
        // - 虽然我们使用 JWT 手动认证，但 Spring Security 内部某些地方仍可能需要它
        // - 例如：密码编码器的比较、SecurityContext 的创建等
        return config.getAuthenticationManager();
    }
}