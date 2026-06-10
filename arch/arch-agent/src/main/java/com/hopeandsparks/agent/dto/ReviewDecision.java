package com.hopeandsparks.agent.dto;

import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.enums.ReviewStatus;

import java.util.List;

public record ReviewDecision(
        ReviewStatus finalDecision,
        String reviewSummary,
        List<String> issues,
        List<String> fixSuggestions,
        AgentName targetRevisionAgent,
        boolean repairable
) {
}
