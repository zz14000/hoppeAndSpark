package com.hopeandsparks.infra.security;

public class InvalidTokenException extends RuntimeException {

    /**
     * 创建 token 解析失败异常。
     * 保留底层 JWT 异常作为 cause，方便日志里继续追踪签名错误、过期或 payload 不合法等具体原因。
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
