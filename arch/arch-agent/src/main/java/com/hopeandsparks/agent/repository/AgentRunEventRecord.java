package com.hopeandsparks.agent.repository;

import java.time.LocalDateTime;

public record AgentRunEventRecord(
        String eventId,
        String runId,
        String nodeName,
        String stage,
        String status,
        String summary,
        String payloadJson,
        long durationMs,
        int retryCount,
        LocalDateTime createdAt
) {
}
