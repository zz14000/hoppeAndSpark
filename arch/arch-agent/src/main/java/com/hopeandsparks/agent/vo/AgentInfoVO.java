package com.hopeandsparks.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "智能体展示信息")
public record AgentInfoVO(
        String id,
        String name,
        String role,
        List<String> tags,
        String icon,
        String welcomeMessage,
        boolean streamEnabled,
        boolean workflow
) {
}
