package com.hopeandsparks.agent.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 智能体列表接口，向前端暴露 Ava、Sage、Coach、Nebula 等可用 Agent。
 *
 * <p>后续会从 {@code sys_agent_config} 和提示词配置中组装展示信息，但这张配置表归 infra 管理。
 * agent 模块只关心会话、消息、记忆和 Prompt 业务入口。</p>
 */
@RestController
public class AgentCatalogController {

    @GetMapping("/api/v1/agents")
    public ApiResponse<Map<String, Object>> agents() {
        return ApiResponse.ok(PlaceholderData.of("agent", "agents"));
    }
}
