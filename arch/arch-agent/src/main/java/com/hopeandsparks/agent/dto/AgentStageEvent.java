package com.hopeandsparks.agent.dto;

import java.util.Map;

public record AgentStageEvent(
        String runId,
        long stateVersion,
        String stage,
        String summary,
        Map<String, Object> payload,
        boolean mock
) {
}
