package com.hopeandsparks.agent.repository;

import java.time.LocalDateTime;

public record AgentProjectMemoryRecord(
        String projectId,
        String userId,
        String courseId,
        String courseName,
        String knowledgePoint,
        int masteryLevel,
        String weaknessTagsJson,
        String lastLearningPlanJson,
        LocalDateTime updatedAt
) {
}
