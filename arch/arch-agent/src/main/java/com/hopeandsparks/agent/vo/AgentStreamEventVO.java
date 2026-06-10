package com.hopeandsparks.agent.vo;

import java.util.Map;

public record AgentStreamEventVO(
        String type,
        String messageId,
        String content,
        Map<String, Object> payload,
        boolean mock
) {
}
