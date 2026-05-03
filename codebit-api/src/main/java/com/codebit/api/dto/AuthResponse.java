package com.codebit.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/3 星期五
 * @Description:
 * @VERSON: 17
 */

@Data
@AllArgsConstructor  // 方便创建响应对象，避免逐个 set
@Schema(description = "认证请求")
public class AuthResponse {

    @Schema(description = "token")
    private String token;      // JWT Token

    @Schema(description = "username")
    private String username;   // 用户名

    @Schema(description = "userId")
    private Long userId;       // 用户ID

    @Schema(description = "expirsIn")
    private Long expiresIn;    // 过期时间（毫秒）


}
