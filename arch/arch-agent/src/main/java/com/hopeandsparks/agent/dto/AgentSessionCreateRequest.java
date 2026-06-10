package com.hopeandsparks.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Schema(description = "创建智能体会话请求")
public record AgentSessionCreateRequest(
        @Schema(description = "智能体ID，如 ava/sage/coach/nebula")
        String agentId,

        @Schema(description = "会话标题")
        @Size(max = 200, message = "会话标题不能超过200个字符")
        String title,

        @Schema(description = "会话来源，如 agent_chat/reading/practice")
        String source,

        @Schema(description = "上下文知识点ID")
        String contextNodeId,

        @Schema(description = "上下文资源ID")
        String contextResourceId,

        @Schema(description = "页面上下文")
        Map<String, Object> context,

        @Schema(description = "扩展元数据")
        Map<String, Object> metadata
) {
}
