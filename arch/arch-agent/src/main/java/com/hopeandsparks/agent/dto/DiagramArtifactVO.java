package com.hopeandsparks.agent.dto;

public record DiagramArtifactVO(
        String diagramType,
        String diagramScript,
        String diagramImagePath,
        String textExplanation
) {
}
