package com.hopeandsparks.agent.dto;

import java.util.Map;

public record AgentCheckpointSnapshot(
        String checkpointId,
        String runId,
        String nodeName,
        long stateVersion,
        String checkpointStateJson,
        Map<String, Object> payload
) {
}
