package com.hopeandsparks.agent.repository;

import java.time.LocalDateTime;

public record AgentRunCheckpointRecord(
        String checkpointId,
        String runId,
        String nodeName,
        long stateVersion,
        String checkpointStateJson,
        String payloadJson,
        LocalDateTime createdAt
) {
}
