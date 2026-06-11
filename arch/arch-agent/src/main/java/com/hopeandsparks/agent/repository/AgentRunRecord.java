package com.hopeandsparks.agent.repository;

import java.time.LocalDateTime;

public record AgentRunRecord(
        String runId,
        String sessionId,
        String userId,
        String projectId,
        String requestJson,
        String runtime,
        String status,
        String currentNode,
        int currentRevision,
        int maxRevision,
        String errorCode,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
