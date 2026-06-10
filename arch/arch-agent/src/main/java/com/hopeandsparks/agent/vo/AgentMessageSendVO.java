package com.hopeandsparks.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "发送消息响应")
public record AgentMessageSendVO(
        String messageId,
        AgentMessageVO reply
) {
}
