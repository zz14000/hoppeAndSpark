package com.hopeandsparks.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@Schema(description = "发送智能体消息请求")
public record AgentMessageSendRequest(
        @Schema(description = "消息正文")
        @NotBlank(message = "消息内容不能为空")
        String content,

        @Schema(description = "消息类型，默认 text")
        String contentType,

        @Schema(description = "附件列表，W3 阶段只做透传记录")
        List<Map<String, Object>> attachments,

        @Schema(description = "是否希望前端走 SSE 流式读取")
        Boolean stream
) {
}
