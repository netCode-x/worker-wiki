package com.codebit.api.dto.authDto;

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
@AllArgsConstructor
@Schema(description = "认证响应")
public class AuthResponse {

    @Schema(description = "token")
    private String token;

    @Schema(description = "username")
    private String username;

    @Schema(description = "userId")
    private Long userId;

    @Schema(description = "expirsIn")
    private Long expiresIn;


}
