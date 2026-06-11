package com.hopeandsparks.agent.dto;

import java.util.List;
import java.util.Map;

public record ResourceSelectionDecision(
        String mode,
        String selectionReason,
        List<String> selectedTypes,
        List<String> qualityFlags,
        Map<String, Object> metadata
) {
}
