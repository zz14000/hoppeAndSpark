package com.hopeandsparks.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SSE 流式事件")
public record AgentStreamEventVO(
        String type,
        String content,
        String messageId,
        boolean mock
) {
}
