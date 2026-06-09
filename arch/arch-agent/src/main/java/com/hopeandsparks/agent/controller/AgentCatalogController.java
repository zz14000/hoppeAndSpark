package com.hopeandsparks.agent.controller;

import com.hopeandsparks.agent.service.AgentCatalogService;
import com.hopeandsparks.agent.vo.AgentInfoVO;
import com.hopeandsparks.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 智能体列表接口，向前端暴露当前可选的 Agent。
 */
@RestController
public class AgentCatalogController {

    private final AgentCatalogService agentCatalogService;

    public AgentCatalogController(AgentCatalogService agentCatalogService) {
        this.agentCatalogService = agentCatalogService;
    }

    @GetMapping("/api/v1/agents")
    public ApiResponse<List<AgentInfoVO>> agents() {
        return ApiResponse.ok(agentCatalogService.listAgents());
    }
}
