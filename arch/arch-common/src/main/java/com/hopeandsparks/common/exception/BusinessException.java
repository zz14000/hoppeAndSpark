package com.hopeandsparks.common.exception;

/**
 * 业务异常，用于表达可预期的业务错误，例如未登录、无权限、资源不存在或状态冲突。
 *
 * <p>Service 层发现业务规则不满足时可以抛出这个异常，并带上接口约定的 code。
 * {@code GlobalExceptionHandler} 会把它转换成统一 {@code ApiResponse}，Controller
 * 不需要重复写 try/catch。</p>
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
