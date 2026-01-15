package com.shiguang.camera.exception;

import com.shiguang.camera.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理权限异常（可以将权限异常转换为业务异常）
     */
    @ExceptionHandler(SecurityException.class)
    public Result<?> handleSecurityException(SecurityException e) {
        log.warn("权限异常: {}", e.getMessage());
        return Result.error(403, e.getMessage());
    }
}