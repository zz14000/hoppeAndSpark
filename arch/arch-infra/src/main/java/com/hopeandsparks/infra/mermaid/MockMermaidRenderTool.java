package com.hopeandsparks.infra.mermaid;

import com.hopeandsparks.infra.config.AiProperties;

import java.nio.file.Path;

public class MockMermaidRenderTool implements MermaidRenderTool {

    private final AiProperties properties;

    public MockMermaidRenderTool(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public MermaidRenderResult render(MermaidRenderRequest request) {
        String format = request.format() == null || request.format().isBlank() ? "png" : request.format();
        String name = request.outputName() == null || request.outputName().isBlank() ? "diagram" : request.outputName();
        String outputPath = Path.of(properties.getMermaid().getOutputDir(), name + "." + format).toString();
        return new MermaidRenderResult(request.diagramScript(), outputPath, format, true);
    }
}
