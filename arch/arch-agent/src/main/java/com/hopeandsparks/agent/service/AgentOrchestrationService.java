package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.vo.AgentRunResultVO;

public interface AgentOrchestrationService {

    AgentRunResultVO run(AgentRunRequest request);
}
