package com.hopeandsparks.interfaces.agent;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：暴露智能体目录接口，后续会从 sys_agent_config/sys_agent_prompt 读取可用 Agent 配置。
 */
@RestController
@RequestMapping("/api/v1/agents")
public class AgentCatalogController {

    @GetMapping
    public ApiResponse<Map<String, Object>> agents(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "agent", "agents");
    }
}
