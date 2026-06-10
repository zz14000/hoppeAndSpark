package com.hopeandsparks.agent.repository;

import java.time.LocalDateTime;

public record AgentSessionMemoryRecord(
        String sessionId,
        String userId,
        String projectId,
        String recentSummary,
        String lastPlanJson,
        String unfinishedTaskJson,
        LocalDateTime updatedAt
) {
}
