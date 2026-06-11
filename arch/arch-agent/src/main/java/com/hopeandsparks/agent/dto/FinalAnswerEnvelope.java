package com.hopeandsparks.agent.dto;

import java.util.List;
import java.util.Map;

public record FinalAnswerEnvelope(
        String conclusion,
        String answerSummary,
        String detailedExplanation,
        List<String> steps,
        DiagramArtifactVO diagram,
        List<CitationVO> citations,
        List<String> learningPlan,
        ResourceBundle resourceBundle,
        String resourceSummary,
        List<String> qualityFlags,
        Map<String, Object> debug
) {
}
