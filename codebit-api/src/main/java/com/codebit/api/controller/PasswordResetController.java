package com.codebit.api.controller;

import com.codebit.api.dto.ForgetPasswordRequest;
import com.codebit.api.dto.ResetPasswordRequest;
import com.codebit.api.dto.Result;
import com.codebit.api.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
@Tag(name = "密码找回")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @RequestMapping(path = "/forget",method = RequestMethod.POST)
    @Operation(summary = "找回密码")
    public Result<Void> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        log.info("收到密码重置请求: email={}", request.getEmail());
        passwordResetService.sendResetCode(request);
        return Result.success(null);
    }

    @RequestMapping(path = "/reset",method = RequestMethod.POST)
    @Operation(summary = "重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("提交密码重置: email={}", request.getEmail());
        passwordResetService.resetPassword(request);
        return Result.success(null);
    }
}