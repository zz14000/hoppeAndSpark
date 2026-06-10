package com.hopeandsparks.agent.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentMessageSendRequest(
        @NotBlank(message = "消息不能为空")
        String content,
        String agentMode,
        String outputPreference,
        String courseId,
        java.util.List<String> knowledgePointIds,
        Boolean allowWebSearch,
        String strictnessLevel,
        Boolean renderMermaid
) {
}
