package com.hopeandsparks.common.response;

import com.hopeandsparks.common.web.RequestContext;

/**
 * 全局统一响应体，前台 Spark 接口和后台 Manage 接口都使用这一层包装。
 *
 * <p>Controller 返回业务数据时统一调用 {@link #ok(Object)} 或 {@link #ok(String, Object)}，
 * 异常场景由全局异常处理器返回 {@link #fail(int, String)}。这样前端永远按
 * code、message、data、requestId 四个字段解析，不需要每个接口单独约定响应格式。</p>
 */
public record ApiResponse<T>(int code, String message, T data, String requestId) {

    /**
     * 构造标准成功响应，requestId 会自动从当前请求上下文中读取。
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data, RequestContext.getRequestId());
    }

    /**
     * 构造带自定义提示文案的成功响应，适合注册成功、提交成功等需要明确提示的接口。
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(200, message, data, RequestContext.getRequestId());
    }

    /**
     * 构造失败响应，通常由 {@code GlobalExceptionHandler} 调用，业务代码一般直接抛异常即可。
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null, RequestContext.getRequestId());
    }
}
