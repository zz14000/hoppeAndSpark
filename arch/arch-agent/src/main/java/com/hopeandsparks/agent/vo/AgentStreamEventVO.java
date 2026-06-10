package com.hopeandsparks.agent.vo;

public record AgentStreamEventVO(
        String type,
        String messageId,
        String content,
        boolean mock
) {
}
