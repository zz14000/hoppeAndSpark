package com.hopeandsparks.manage.vo;

import java.time.LocalDateTime;
import java.util.Map;

public record AgentRunVO(
        String runId,
        String sessionId,
        String userId,
        String projectId,
        String runtime,
        String status,
        String currentNode,
        int currentRevision,
        int maxRevision,
        String errorCode,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Map<String, Object> summary
) {
}
