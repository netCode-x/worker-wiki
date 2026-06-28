package com.codebit.api.config;

import com.codebit.api.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/4 星期六
 * @Description:
 * @VERSON: 17
 */


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;  // 自定义的用户详情服务

    /**
     * 重写 shouldNotFilter 方法（更优雅） ,  核心作用是告诉securtychainfilter 放行, 不再继续接下来的校验
     * @param request
     * @return
     * @throws ServletException
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

       // String path = request.getRequestURI();
        // 使用 getServletPath() 确保匹配的是应用内部的路径，不受部署上下文影响
        String path = request.getServletPath();
        return path.equals("/api/auth/register") || path.startsWith("/public/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        // 1. 从请求头中获取 Authorization ；标准 JWT 携带方式: Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");

        // 检查是否存在且格式正确（以 "Bearer " 开头）
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 去掉 "Bearer " 前缀（7 个字符），只保留 Token 部分
            String token = authHeader.substring(7);

            // 2. 验证 Token 是否有效
            if (jwtUtil.validateToken(token)) {
                // 从 Token 中提取用户名
                String username = jwtUtil.getUsernameFromToken(token);

                // 3. 如果用户名存在且当前上下文中还没有认证信息
                // SecurityContextHolder.getContext().getAuthentication() == null
                // 确保不会重复设置认证（避免多次过滤时重复）
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 加载用户详情（包含密码、权限等）
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 4. 创建认证 Token
                    // 参数：principal（用户信息）、credentials（凭证，设为 null）、authorities（权限列表）
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // 设置请求详情（IP、Session ID 等，用于审计）
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. 将认证信息放入 SecurityContextHolder
                    // 这一步是关键：Spring Security 后续通过 SecurityContextHolder 获取当前用户
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("用户认证成功: {}", username);
                }
            } else {
                log.warn("无效的 JWT Token");
                // 注意：这里不直接返回 401，因为可能还有其他认证方式
                // 交由后续过滤器处理，如果没有认证信息最终会被拒绝
            }
        }

        // 6. 继续执行后续过滤器链
        // 无论 Token 是否有效，都要调用 filterChain.doFilter
        // 如果 Token 无效且没有其他认证信息，Spring Security 最终会返回 401
        filterChain.doFilter(request, response);
    }
}

