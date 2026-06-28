package com.codebit.api.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * @Auther: yangkaihu  插件 Atom Material Icons
 * @Date: 2026/4/3 星期五
 * @Description:
 * @VERSON: 17
 */


@Slf4j
@Component
public class JwtUtil {

    // @Value: 从 application.yml 中读取 jwt.secret 配置，如果没有则使用默认值
    @Value("${jwt.secret:mySecretKeyForJwtTokenGeneration2026SpringBoot}")
    private String secret;

    // 默认 24 小时 = 86400000 毫秒
    @Value("${jwt.expiration:86400000}")
    private Long expiration;


    /**
     * 生成 JWT Token
     * @param userId 用户ID（作为 Token 的主题 Subject）
     * @param username 用户名（作为自定义 Claim 存储）
     */
    public String generateToken(Long userId, String username) {

        Date now = new Date(); // 当前时间
        Date expiryDate = new Date(now.getTime() + expiration);  // 过期时间 = 当前时间 + 有效期

        // 将字符串密钥转换为 HMAC-SHA256 算法所需的 SecretKey 对象
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // 使用 JJWT 构建器创建 Token
        return Jwts.builder()
                .setSubject(String.valueOf(userId))  // 设置主题（通常存放用户唯一标识）
                .claim(
                        "username", username
                        )     // 添加自定义声明（claim）, 存储额外信息
                .setIssuedAt(now)                 // 设置签发时间，（用于判断token何时生效）
                .setExpiration(expiryDate)         //设置过期时间（服务端校验时会自动检查）
                .signWith(key, SignatureAlgorithm.HS256)  // 使用HS256 算法签名，防止token 被篡改
                .compact(); // 压缩生成最终的token 字符串，（形如： xxxx.yyyyy.zzzzz）
    }

    /**
     * 从 Token 中获取用户ID
     * 调用此方法前需确保 Token 已验证有效
     */
    public Long  getUserIdFromToken(String token) {
        Claims claims = parseToken(token);  // 解析 Token 获取 Claims（负载）
        return Long.parseLong(claims.getSubject());  // Subject 中存储的是用户ID
    }
    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("username");  // 取出之前存放的自定义 Claim
    }
    /**
     * 验证 Token 是否有效
     * 检查：签名正确、未过期、格式正确
     * @return true: 有效；false: 无效（具体原因通过日志记录）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);  // 能正常解析说明签名正确且未过期
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的 Token 格式: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Token 格式错误: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Token 签名验证失败: {}", e.getMessage());  // 可能被篡改
        } catch (IllegalArgumentException e) {
            log.error("Token 参数异常: {}", e.getMessage());
        }
        return false;
    }
    /**
     * 解析 Token，获取 Claims（负载）
     * 如果 Token 无效（签名错误、过期等）会抛出对应异常
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)  // 使用相同的密钥验证签名
                .build()
                .parseClaimsJws(token)  // 解析并验证 JWT
                .getBody();  // 获取负载部分（Payload/Claims）
    }
    /**
     * 获取 Token 的剩余有效期（毫秒）
     * 可用于前端实现 Token 自动刷新逻辑
     */
    public Long getExpirationRemaining(String token) {
        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        Date now = new Date();
        return expiration.getTime() - now.getTime();  // 正数表示还有效，负数表示已过期
    }
}
