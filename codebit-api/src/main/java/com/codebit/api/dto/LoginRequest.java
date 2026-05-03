package com.codebit.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/3 星期五
 * @Description:
 * @VERSON: 17
 */

@Data
@Schema(description = "登录请求")
public class LoginRequest {

      @Schema(description = "用户名")
      @NotBlank(message = "用户名不能为空")
      private String username;

      @Schema(description = "用户密码")
      @NotBlank(message = "密码不能为空")
      private String password;

}
