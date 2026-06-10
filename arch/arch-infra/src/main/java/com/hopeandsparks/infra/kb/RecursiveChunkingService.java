package com.hopeandsparks.infra.kb;

import com.hopeandsparks.infra.config.KbProperties;
import com.hopeandsparks.infra.embedding.EmbeddingGateway;
import com.hopeandsparks.infra.embedding.EmbeddingRequest;
import com.hopeandsparks.infra.embedding.EmbeddingResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecursiveChunkingService implements ChunkingService {

    private final KbProperties.Chunk properties;
    private final EmbeddingGateway embeddingGateway;

    public RecursiveChunkingService(KbProperties.Chunk properties, EmbeddingGateway embeddingGateway) {
        this.properties = properties;
        this.embeddingGateway = embeddingGateway;
    }

    @Override
    public ChunkedDocument chunk(ParsedDocument document) {
        List<DocumentChunk> chunks = new ArrayList<>();
        int index = 0;
        for (ParsedSection section : document.sections()) {
            List<String> blocks = semanticBlocks(section.content());
            for (String block : blocks) {
                for (String window : windows(block)) {
                    Map<String, Object> metadata = new LinkedHashMap<>();
                    metadata.put("sectionTitle", section.title());
                    metadata.put("level", section.level());
                    chunks.add(new DocumentChunk(index++, window, section.path(), estimateTokens(window), metadata));
                }
            }
        }
        return new ChunkedDocument(document.title(), chunks, document.metadata());
    }

    private List<String> semanticBlocks(String content) {
        List<String> paragraphs = splitParagraphs(content);
        if (!properties.isEnableSemantic() || paragraphs.size() <= 1) {
            return List.of(content);
        }
        EmbeddingResponse response = embeddingGateway.embed(new EmbeddingRequest(paragraphs, Map.of("stage", "kb-semantic-chunk")));
        if (response.vectors().size() != paragraphs.size()) {
            return List.of(content);
        }
        List<String> blocks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        List<Float> previous = null;
        for (int i = 0; i < paragraphs.size(); i++) {
            String paragraph = paragraphs.get(i);
            List<Float> vector = response.vectors().get(i);
            boolean split = previous != null && cosine(previous, vector) < properties.getSemanticThreshold();
            if (split && current.length() > 0) {
                blocks.add(current.toString().trim());
                current.setLength(0);
            }
            current.append(paragraph).append("\n\n");
            previous = vector;
        }
        if (current.length() > 0) {
            blocks.add(current.toString().trim());
        }
        return blocks.isEmpty() ? List.of(content) : blocks;
    }

    private List<String> windows(String content) {
        int max = Math.max(200, properties.getMaxCharacters());
        int overlap = Math.max(0, Math.min(properties.getOverlapCharacters(), max / 2));
        if (content.length() <= max) {
            return List.of(content);
        }
        List<String> windows = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(content.length(), start + max);
            if (end < content.length()) {
                int breakPoint = Math.max(content.lastIndexOf('\n', end), content.lastIndexOf('。', end));
                if (breakPoint > start + max / 2) {
                    end = breakPoint + 1;
                }
            }
            windows.add(content.substring(start, end).trim());
            if (end >= content.length()) {
                break;
            }
            start = Math.max(end - overlap, start + 1);
        }
        return windows;
    }

    private List<String> splitParagraphs(String content) {
        return List.of(content.split("\\n\\s*\\n"))
                .stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private double cosine(List<Float> left, List<Float> right) {
        double dot = 0D;
        double leftNorm = 0D;
        double rightNorm = 0D;
        for (int i = 0; i < Math.min(left.size(), right.size()); i++) {
            double l = left.get(i);
            double r = right.get(i);
            dot += l * r;
            leftNorm += l * l;
            rightNorm += r * r;
        }
        if (leftNorm == 0D || rightNorm == 0D) {
            return 0D;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private int estimateTokens(String value) {
        return Math.max(1, value.length() / 3);
    }
}
