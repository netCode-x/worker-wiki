package com.codebit.api.controller;

import com.codebit.api.dto.AuthResponse;
import com.codebit.api.dto.LoginRequest;
import com.codebit.api.dto.RegisterRequest;
import com.codebit.api.dto.Result;
import com.codebit.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "身份认证")
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册接口
     * POST /api/auth/register
     *
     * @Valid 触发 RegisterRequest 中的校验注解（@NotBlank、@Size 等）
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @Operation(summary = "用户注册姐")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("注册请求参数: username={}, email={}", request.getUsername(), request.getEmail());
        AuthResponse response = userService.register(request);
        return Result.success(response);
    }

    /**
     * 用户登录接口
     * POST /api/auth/login
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @Operation(summary = "用户登录接口")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return Result.success(response);
    }

}