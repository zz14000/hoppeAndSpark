package com.hopeandsparks.infra.coze;

import java.util.Map;

/**
 * 调用 Coze Bot 或 Workflow 的基础请求。
 * 参数先用 Map 承载，避免 W1 就把 Agent 协议写得太复杂。
 */
public record CozeAgentRequest(
        String agentCode,
        String input,
        String conversationId,
        Map<String, String> context
) {
}
