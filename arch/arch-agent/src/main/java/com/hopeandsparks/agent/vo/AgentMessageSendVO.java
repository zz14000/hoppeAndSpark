package com.hopeandsparks.agent.vo;

public record AgentMessageSendVO(
        String messageId,
        String sessionId,
        String finalAnswer,
        String diagramScript,
        String diagramImagePath,
        String runId,
        boolean mock
) {
}
