package com.hopeandsparks.infra.redis;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Stream 消息的简单视图。
 * requestId 会跟着消息走，方便以后排查异步链路。
 */
public record RedisStreamMessage(
        String messageId,
        String streamKey,
        Map<String, String> body,
        String requestId,
        LocalDateTime createdAt
) {
}
