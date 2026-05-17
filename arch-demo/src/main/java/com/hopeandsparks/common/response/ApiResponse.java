package com.hopeandsparks.common.response;


/**
 * 文件职责：ApiResponse 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\common\response\ApiResponse.java，用于承载对应分层或接口的基础职责。
 */
public record ApiResponse<T>(int code, String message, T data, String requestId) {

    public static <T> ApiResponse<T> success(T data, String requestId) {
        return new ApiResponse<>(200, "success", data, requestId);
    }

    public static <T> ApiResponse<T> fail(int code, String message, String requestId) {
        return new ApiResponse<>(code, message, null, requestId);
    }
}

