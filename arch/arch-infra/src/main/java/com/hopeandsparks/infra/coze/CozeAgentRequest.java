package com.hopeandsparks.infra.coze;

import java.util.Map;

/**
 * Legacy compatibility DTO. Coze is no longer the primary runtime path.
 */
public record CozeAgentRequest(
        String agentKey,
        String conversationId,
        String userQuery,
        Map<String, Object> payload
) {
}
