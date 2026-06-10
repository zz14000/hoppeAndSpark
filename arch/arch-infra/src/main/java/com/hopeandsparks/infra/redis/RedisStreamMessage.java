package com.hopeandsparks.infra.redis;

import java.time.LocalDateTime;
import java.util.Map;

public record RedisStreamMessage(
        String messageId,
        String streamKey,
        Map<String, String> body,
        LocalDateTime createdAt
) {
}
