package com.hopeandsparks.agent.dto;

import java.util.List;

public record ResourceSearchRequest(
        String userId,
        String projectId,
        String query,
        List<String> knowledgePointIds,
        List<String> preferredResourceTypes,
        boolean allowWebSearch,
        int topK
) {
}
