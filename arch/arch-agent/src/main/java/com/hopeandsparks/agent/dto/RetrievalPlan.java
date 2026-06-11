package com.hopeandsparks.agent.dto;

import java.util.List;

public record RetrievalPlan(
        String originalQuery,
        List<String> rewrittenQueries,
        boolean webFallbackAllowed,
        boolean webFallbackTriggered,
        int maxContextChunks
) {
}
