package com.hopeandsparks.agent.dto;

import java.util.List;
import java.util.Map;

public record MemoryContext(
        String sessionSummary,
        Map<String, Object> sessionState,
        String projectSummary,
        Map<String, Object> projectState,
        List<String> memoryUpdates
) {
}
