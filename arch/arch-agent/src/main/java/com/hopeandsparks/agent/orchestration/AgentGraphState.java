package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.ReviewDecision;
import com.hopeandsparks.agent.enums.AgentIntent;

import java.util.List;
import java.util.Map;

/**
 * State object for the future LangGraph4j graph. The current skeleton executes
 * the same stages directly so the contract is stable before graph wiring.
 */
public record AgentGraphState(
        AgentRunRequest request,
        AgentIntent intent,
        List<AgentTask> tasks,
        List<AgentTaskResult> taskResults,
        ReviewDecision reviewDecision,
        Map<String, Object> memoryContext,
        Map<String, Object> ragContext,
        Map<String, Object> payload
) {
}
