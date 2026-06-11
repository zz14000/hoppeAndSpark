package com.hopeandsparks.agent.dto;

import java.util.Map;

public record AgentStructuredOutput(
        String schemaVersion,
        String outputType,
        Map<String, Object> payload
) {
}
