package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentExecutionPlan;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.MemoryContext;
import com.hopeandsparks.agent.dto.RetrievalBundle;
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
        List<String> subIntents,
        AgentExecutionPlan plan,
        List<AgentTask> tasks,
        List<AgentTaskResult> specialistResults,
        ReviewDecision review,
        MemoryContext memory,
        RetrievalBundle retrieval,
        String draft,
        Map<String, Object> revision,
        Map<String, Object> toolContext,
        Map<String, Object> artifacts,
        Map<String, Object> telemetry,
        int maxRevisionCount,
        int currentRevisionCount,
        long stateVersion,
        Map<String, Object> payload
) {
}
