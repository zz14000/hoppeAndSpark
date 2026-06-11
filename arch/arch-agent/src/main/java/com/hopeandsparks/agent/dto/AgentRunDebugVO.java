package com.hopeandsparks.agent.dto;

import java.util.Map;

public record AgentRunDebugVO(
        String runtime,
        int currentRevisionCount,
        Map<String, Object> retrievalDebug,
        Map<String, Object> toolContext
) {
}
