package com.hopeandsparks.infra.mermaid;

public record MermaidRenderResult(String diagramScript, String outputPath, String format, boolean mock) {
}
