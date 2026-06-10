package com.hopeandsparks.infra.coze;

/**
 * Coze 调用结果。
 * mock=true 表示当前没有真实接入外部 Agent。
 */
public record CozeAgentResponse(
        String agentCode,
        String output,
        String externalConversationId,
        String externalMessageId,
        boolean mock
) {
}
