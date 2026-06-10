package com.hopeandsparks.infra.mermaid;

import com.hopeandsparks.infra.config.AiProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;

public class CliMermaidRenderTool implements MermaidRenderTool {

    private final AiProperties properties;

    public CliMermaidRenderTool(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public MermaidRenderResult render(MermaidRenderRequest request) {
        try {
            String format = normalizeFormat(request.format());
            String name = safeFileName(request.outputName());
            Path outputDir = Path.of(properties.getMermaid().getOutputDir());
            Files.createDirectories(outputDir);

            Path scriptPath = outputDir.resolve(name + ".mmd");
            Path imagePath = outputDir.resolve(name + "." + format);
            Files.writeString(scriptPath, request.diagramScript() == null ? "" : request.diagramScript(), StandardCharsets.UTF_8);

            String command = findMermaidCli();
            if (command.isBlank()) {
                return new MermaidRenderResult(request.diagramScript(), scriptPath.toString(), "mmd", true);
            }

            Process process = new ProcessBuilder(command, "-i", scriptPath.toString(), "-o", imagePath.toString())
                    .redirectErrorStream(true)
                    .start();
            boolean completed = process.waitFor(Duration.ofSeconds(30).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                return new MermaidRenderResult(request.diagramScript(), scriptPath.toString(), "mmd", true);
            }
            if (process.exitValue() != 0 || !Files.exists(imagePath)) {
                return new MermaidRenderResult(request.diagramScript(), scriptPath.toString(), "mmd", true);
            }
            return new MermaidRenderResult(request.diagramScript(), imagePath.toString(), format, false);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Mermaid render failed: " + exception.getMessage(), exception);
        }
    }

    private String normalizeFormat(String format) {
        String value = format == null || format.isBlank() ? "png" : format.toLowerCase(Locale.ROOT);
        return ("svg".equals(value) || "png".equals(value)) ? value : "png";
    }

    private String safeFileName(String outputName) {
        String value = outputName == null || outputName.isBlank() ? "diagram" : outputName;
        return value.replaceAll("[^a-zA-Z0-9._-]", "-");
    }

    private String findMermaidCli() {
        String configured = System.getenv("MERMAID_CLI");
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        if (isCommandAvailable("mmdc")) {
            return "mmdc";
        }
        if (isCommandAvailable("mmdc.cmd")) {
            return "mmdc.cmd";
        }
        return "";
    }

    private boolean isCommandAvailable(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version")
                    .redirectErrorStream(true)
                    .start();
            boolean completed = process.waitFor(Duration.ofSeconds(5).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}
