package com.codebit.api.dto.likeDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LikeResponse {
    private Long likeCount;
    private String message;
    private Boolean success;

    @Data
    public static class ResetPasswordRequest {


        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "验证码不能为空")
        private String emailCode;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度为6-20位")
        private String newPassword;
    }
}