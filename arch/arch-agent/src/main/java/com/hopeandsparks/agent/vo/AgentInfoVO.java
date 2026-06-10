package com.hopeandsparks.agent.vo;

import java.util.List;

public record AgentInfoVO(
        String agentKey,
        String name,
        String description,
        List<String> capabilities,
        boolean mock
) {
}
