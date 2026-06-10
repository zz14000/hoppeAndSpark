package com.hopeandsparks.infra.tool;

import com.hopeandsparks.infra.config.AiProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocalKnowledgeCacheGateway implements KnowledgeCacheGateway {

    private final AiProperties properties;

    public LocalKnowledgeCacheGateway(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, Object> writeCandidate(Map<String, Object> input) {
        try {
            Path outputDir = Path.of(properties.getChroma().getLocalCacheDir());
            Files.createDirectories(outputDir);
            Path outputFile = outputDir.resolve("web_cache_candidates.jsonl");
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("writtenAt", LocalDateTime.now().toString());
            record.putAll(input == null ? Map.of() : input);
            Files.writeString(outputFile, toJsonLine(record), StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
            return Map.of("status", "written", "path", outputFile.toString());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write knowledge cache candidate: " + exception.getMessage(), exception);
        }
    }

    private String toJsonLine(Map<String, Object> record) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : record.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append('"').append(':')
                    .append('"').append(escape(String.valueOf(entry.getValue()))).append('"');
        }
        return builder.append('}').append(System.lineSeparator()).toString();
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
