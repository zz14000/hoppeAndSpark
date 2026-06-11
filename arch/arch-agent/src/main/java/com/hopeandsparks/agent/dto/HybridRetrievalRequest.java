package com.hopeandsparks.agent.dto;

import com.hopeandsparks.agent.enums.AgentRetrievalMode;

import java.util.List;

public record HybridRetrievalRequest(
        String userId,
        String projectId,
        String query,
        List<String> knowledgePointIds,
        boolean allowWebSearch,
        AgentRetrievalMode retrievalMode,
        int topK
) {
}
