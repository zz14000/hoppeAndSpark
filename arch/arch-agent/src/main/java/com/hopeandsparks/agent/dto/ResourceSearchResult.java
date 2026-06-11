package com.hopeandsparks.agent.dto;

import java.util.List;
import java.util.Map;

public record ResourceSearchResult(
        ResourceBundle resourceBundle,
        ResourceSelectionDecision selectionDecision,
        List<String> candidateIds,
        List<String> qualityFlags,
        Map<String, Object> debug
) {
}
