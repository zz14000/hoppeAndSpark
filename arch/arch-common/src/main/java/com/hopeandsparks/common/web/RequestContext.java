package com.hopeandsparks.common.web;

import java.util.UUID;

/**
 * 请求上下文，当前主要保存 {@code X-Request-Id}。
 *
 * <p>一次 HTTP 请求进入系统后，RequestIdFilter 会把请求 ID 放到这里；
 * 统一响应、日志、Redis Stream 消息、Coze 调用都可以读取它，实现同一链路的排查追踪。</p>
 */
public final class RequestContext {

    private static final ThreadLocal<String> REQUEST_ID = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    private RequestContext() {
    }

    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static void clear() {
        REQUEST_ID.remove();
    }
}
