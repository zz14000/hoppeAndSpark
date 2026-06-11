package com.hopeandsparks.agent.dto;

import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.enums.AgentNodeStatus;
import com.hopeandsparks.agent.enums.RevisionTarget;
import com.hopeandsparks.agent.enums.ReviewStatus;

import java.util.List;
import java.util.Map;

public record ReviewDecision(
        ReviewStatus finalDecision,
        String reviewSummary,
        List<String> issues,
        List<String> fixSuggestions,
        AgentName targetRevisionAgent,
        RevisionTarget revisionTarget,
        boolean repairable,
        List<String> qualityFlags,
        List<ReviewIssueVO> reviewIssues,
        List<String> resourceIssues,
        RevisionTarget resourceRevisionTarget,
        AgentNodeStatus nodeStatus,
        Map<String, Object> metadata
) {
}
