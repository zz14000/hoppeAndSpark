package com.hopeandsparks.agent.repository;

import java.time.LocalDateTime;

public record AgentMemorySnapshotRecord(
        String runId,
        String sessionId,
        String userId,
        String projectId,
        String courseId,
        String knowledgePoint,
        String memoryLevel,
        String payloadJson,
        LocalDateTime createdAt
) {
}
