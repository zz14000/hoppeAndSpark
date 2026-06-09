package com.hopeandsparks.common.exception;

import com.hopeandsparks.common.response.ApiResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，把框架异常和业务异常统一转换成 {@code ApiResponse}。
 *
 * <p>这样 Controller 和 Service 可以专心处理业务流程，参数校验失败、业务异常、
 * 未预期异常都会在这里变成统一响应结构。后续接入日志、告警、错误码映射时，也优先在这里扩展。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "Invalid request parameters" : error.getDefaultMessage())
                .orElse("Invalid request parameters");
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ApiResponse<Void> handleBadRequest(Exception exception) {
        return ApiResponse.fail(400, "请求参数错误");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnexpectedException(Exception exception) {
        return ApiResponse.fail(500, "Internal server error");
    }
}
