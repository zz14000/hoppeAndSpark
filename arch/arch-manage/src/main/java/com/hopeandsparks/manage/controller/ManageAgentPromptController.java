package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 后台 Prompt 配置接口，负责查看和更新智能体提示词版本。
 *
 * <p>{@code sys_agent_prompt} 归 agent 业务使用，用来保存提示词和模型参数版本；
 * Coze Bot / Workflow 路由配置不在这里维护，而是通过 infra 管理 {@code sys_agent_config}。</p>
 */
@RestController
@RequestMapping("/api/v1/manage/agent-prompts")
public class ManageAgentPromptController {

    @GetMapping
    public ApiResponse<Map<String, Object>> prompts(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "agentPrompts", values("query", query)));
    }

    @PutMapping("/{promptId}")
    public ApiResponse<Map<String, Object>> updatePrompt(
            @PathVariable String promptId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "updateAgentPrompt", values("promptId", promptId, "request", request)));
    }
}
