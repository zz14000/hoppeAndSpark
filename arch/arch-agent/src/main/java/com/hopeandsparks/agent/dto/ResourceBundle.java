package com.hopeandsparks.agent.dto;

import java.util.List;

public record ResourceBundle(
        ResourceItem primaryDiagram,
        List<ResourceItem> videoResources,
        List<ResourceItem> referenceResources,
        List<ResourceItem> practiceResources,
        List<String> qualityFlags,
        String selectionReason
) {
}
