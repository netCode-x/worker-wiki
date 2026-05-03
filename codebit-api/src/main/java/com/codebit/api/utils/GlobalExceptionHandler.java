package com.codebit.api.utils;

import com.codebit.api.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/2 星期四
 * @Description:
 * @VERSON: 17
 */

@Slf4j
@RestControllerAdvice   // 相当于 @ControllerAdvice  +@ResponseBody
public class GlobalExceptionHandler {


    /**
     * 处理自定义业务异常  （重要）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleException(BusinessException e) {
        log.error("业务异常: code={},msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理@Valid 校验异常（用户@RequestBody）
     **/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验失败: {}", errorMsg);
        return Result.error(400, errorMsg);
    }

    /**
     * 处理 @Validated 校验异常（用于 @RequestParam 等）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验失败: {}", errorMsg);
        return Result.error(400, errorMsg);
    }

    /**
     * 处理表单绑定异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String errorMsg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定失败: {}", errorMsg);
        return Result.error(400, errorMsg);
    }

    /**
     * 处理其他未捕获的异常（兜底）
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统内部错误", e);
        return Result.error(500, "系统内部错误，请稍后重试");
    }


}
