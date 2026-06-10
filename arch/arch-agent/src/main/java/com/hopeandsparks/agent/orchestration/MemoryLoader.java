package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.MemoryContext;
import com.hopeandsparks.agent.service.AgentMemoryService;
import org.springframework.stereotype.Component;

@Component
public class MemoryLoader {

    private final AgentMemoryService agentMemoryService;

    public MemoryLoader(AgentMemoryService agentMemoryService) {
        this.agentMemoryService = agentMemoryService;
    }

    public MemoryContext load(AgentRunRequest request) {
        return agentMemoryService.load(request);
    }
}
