package com.hopeandsparks.infra.coze;

import java.util.Map;

/**
 * Legacy compatibility DTO. New agent runtime should use LangChain4j/LangGraph4j ports.
 */
public record CozeAgentResponse(
        String externalRunId,
        String content,
        boolean mock,
        Map<String, Object> raw
) {
}
