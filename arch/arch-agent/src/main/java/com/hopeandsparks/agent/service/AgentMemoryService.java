package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.AgentExecutionPlan;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.MemoryContext;
import com.hopeandsparks.agent.dto.ReviewDecision;

import java.util.List;

public interface AgentMemoryService {

    MemoryContext load(AgentRunRequest request);

    List<String> persist(AgentRunRequest request, AgentExecutionPlan plan, List<AgentTaskResult> results, ReviewDecision reviewDecision);
}
