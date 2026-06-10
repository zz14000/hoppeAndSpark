package com.hopeandsparks.agent.vo;

public record AgentMessageSendVO(
        String messageId,
        String sessionId,
        String answerText,
        String diagramScript,
        String diagramImagePath,
        boolean mock
) {
}
