package com.codebit.api.controller;

import com.codebit.api.dto.aboutDto.*;
import com.codebit.api.dto.likeDto.LikeResponse;
import com.codebit.api.service.impl.AboutServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/about")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "关于")
public class AboutController {

    private final AboutServiceImpl aboutService;

    // ==================== 公开接口 ====================

    @RequestMapping(path = "/getabout", method = RequestMethod.GET)
    @Operation(summary = "获取关于的信息")
    public ResponseEntity<AboutResponse> getAbout() {
        return ResponseEntity.ok(aboutService.getAbout());
    }

    @RequestMapping(path = "/view",method = RequestMethod.POST)
    @Operation(summary = "请求视图")
    public ResponseEntity<Void> incrementViewCount() {
        aboutService.incrementViewCount();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @Operation(summary = "喜欢")
    public ResponseEntity<LikeResponse> incrementLikeCount() {
        return ResponseEntity.ok(aboutService.incrementLikeCount());
    }

    // ==================== 管理端接口 ====================

    @GetMapping("/admin")
    public ResponseEntity<AboutAdminResponse> getAboutForAdmin(
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        validateAdminToken(token);
        return ResponseEntity.ok(aboutService.getAboutForAdmin());
    }

    @PutMapping("/admin")
    public ResponseEntity<Void> updateAbout(
            @Valid @RequestBody AboutUpdateRequest request,
            @RequestHeader("X-Admin-Token") String token) {
        validateAdminToken(token);
        aboutService.updateAbout(request, getCurrentAdminUser(token));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/history")
    public ResponseEntity<List<AboutHistoryResponse>> getVersionHistory(
            @RequestHeader("X-Admin-Token") String token) {
        validateAdminToken(token);
        return ResponseEntity.ok(aboutService.getVersionHistory());
    }

    @PostMapping("/admin/rollback/{version}")
    public ResponseEntity<Void> rollbackToVersion(
            @PathVariable Integer version,
            @RequestHeader("X-Admin-Token") String token) {
        validateAdminToken(token);
        aboutService.rollbackToVersion(version, getCurrentAdminUser(token));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/statistics")
    public ResponseEntity<AboutStatisticsResponse> getStatistics(
            @RequestHeader("X-Admin-Token") String token) {
        validateAdminToken(token);
        return ResponseEntity.ok(aboutService.getStatistics());
    }

    // ==================== 健康检查 ====================

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "about-page");
        return ResponseEntity.ok(status);
    }

    // ==================== 私有辅助方法 ====================

    private void validateAdminToken(String token) {
        String validToken = System.getenv("ABOUT_ADMIN_TOKEN");
        if (validToken == null || validToken.isBlank()) {
            validToken = "default-secure-token-change-me";
        }
        if (token == null || !validToken.equals(token)) {
            throw new RuntimeException("无权限访问");
        }
    }

    private String getCurrentAdminUser(String token) {
        // 可以从 token 解析，这里简单返回
        return "admin";
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("请求处理失败: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}