package com.hopeandsparks.infra.tool;

import com.hopeandsparks.infra.chroma.ChromaVectorStoreGateway;
import com.hopeandsparks.infra.chroma.VectorSearchRequest;
import com.hopeandsparks.infra.mermaid.MermaidRenderRequest;
import com.hopeandsparks.infra.mermaid.MermaidRenderTool;
import com.hopeandsparks.infra.rerank.RerankGateway;
import com.hopeandsparks.infra.rerank.RerankRequest;
import com.hopeandsparks.infra.search.WebSearchGateway;
import com.hopeandsparks.infra.search.WebSearchRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultToolRegistry implements ToolRegistry {

    private final ChromaVectorStoreGateway chroma;
    private final WebSearchGateway webSearch;
    private final RerankGateway rerank;
    private final MermaidRenderTool mermaid;
    private final KnowledgeCacheGateway knowledgeCache;
    private final List<ToolCallRecord> calls = new ArrayList<>();

    public DefaultToolRegistry(
            ChromaVectorStoreGateway chroma,
            WebSearchGateway webSearch,
            RerankGateway rerank,
            MermaidRenderTool mermaid,
            KnowledgeCacheGateway knowledgeCache
    ) {
        this.chroma = chroma;
        this.webSearch = webSearch;
        this.rerank = rerank;
        this.mermaid = mermaid;
        this.knowledgeCache = knowledgeCache;
    }

    @Override
    public Object call(String toolName, Map<String, Object> input) {
        long start = System.currentTimeMillis();
        try {
            Object result = switch (toolName) {
                case "kb_search" -> chroma.search(new VectorSearchRequest(
                        string(input, "userId"), string(input, "projectId"), string(input, "collection"),
                        string(input, "query"), List.of(), intValue(input, "topK", 5), Map.of()));
                case "web_search" -> webSearch.search(new WebSearchRequest(string(input, "query"), intValue(input, "topK", 5), Map.of()));
                case "rerank" -> rerank.rerank(new RerankRequest(string(input, "query"), documents(input), intValue(input, "topK", 1), Map.of()));
                case "mermaid_render" -> mermaid.render(new MermaidRenderRequest(string(input, "diagramScript"), string(input, "outputName"), string(input, "format")));
                case "memory_read" -> Map.of("summary", "mock memory read");
                case "memory_write" -> knowledgeCache.writeCandidate(input);
                default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
            };
            record(toolName, input, result, start, true, "");
            return result;
        } catch (RuntimeException exception) {
            record(toolName, input, null, start, false, exception.getMessage());
            throw exception;
        }
    }

    @Override
    public List<ToolCallRecord> recentCalls() {
        return List.copyOf(calls);
    }

    private void record(String toolName, Map<String, Object> input, Object output, long start, boolean success, String reason) {
        calls.add(new ToolCallRecord(toolName, summarize(input), summarize(output),
                System.currentTimeMillis() - start, success, reason, LocalDateTime.now()));
    }

    private String summarize(Object value) {
        String text = String.valueOf(value);
        int maxLength = 500;
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private String string(Map<String, Object> input, String key) {
        Object value = input == null ? null : input.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private int intValue(Map<String, Object> input, String key, int defaultValue) {
        Object value = input == null ? null : input.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private List<String> documents(Map<String, Object> input) {
        Object documents = input == null ? null : input.get("documents");
        if (documents instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of(string(input, "document"));
    }
}
