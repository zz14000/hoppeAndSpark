package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.vo.AgentRunResultVO;
import com.hopeandsparks.agent.vo.AgentStreamEventVO;

import java.util.List;

public interface AgentOrchestrationService {

    AgentRunResultVO run(AgentRunRequest request);

    List<AgentStreamEventVO> streamEvents(String runId);
}
