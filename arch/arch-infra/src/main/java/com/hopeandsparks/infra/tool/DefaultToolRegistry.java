package com.hopeandsparks.infra.tool;

import com.hopeandsparks.infra.chroma.ChromaVectorStoreGateway;
import com.hopeandsparks.infra.chroma.VectorSearchRequest;
import com.hopeandsparks.infra.mermaid.MermaidRenderRequest;
import com.hopeandsparks.infra.mermaid.MermaidRenderTool;
import com.hopeandsparks.infra.search.WebSearchResponse;
import com.hopeandsparks.infra.rerank.RerankGateway;
import com.hopeandsparks.infra.rerank.RerankRequest;
import com.hopeandsparks.infra.search.WebSearchGateway;
import com.hopeandsparks.infra.search.WebSearchRequest;
import com.hopeandsparks.infra.search.WebSearchResult;

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
                        string(input, "userId"), string(input, "projectId"), string(input, "collection"), collections(input, "collections"),
                        string(input, "query"), List.of(), intValue(input, "topK", 5), Map.of()));
                case "keyword_search" -> Map.of(
                        "query", string(input, "query"),
                        "collections", collections(input, "collections"),
                        "topK", intValue(input, "topK", 5)
                );
                case "hybrid_retrieval" -> Map.of(
                        "query", string(input, "query"),
                        "mode", string(input, "mode"),
                        "topK", intValue(input, "topK", 5)
                );
                case "checkpoint_read" -> input;
                case "checkpoint_write" -> input;
                case "web_search" -> webSearch.search(new WebSearchRequest(string(input, "query"), intValue(input, "topK", 5), Map.of()));
                case "resource_search" -> webSearch.search(new WebSearchRequest(
                        string(input, "query"),
                        intValue(input, "topK", 5),
                        Map.of("resourceType", string(input, "resourceType"))
                ));
                case "video_search" -> filterVideos(webSearch.search(new WebSearchRequest(
                        withSiteFilter(string(input, "query"), collections(input, "platforms")),
                        intValue(input, "topK", 5),
                        Map.of("resourceType", "video", "platforms", collections(input, "platforms"))
                )), collections(input, "platforms"));
                case "rerank" -> rerank.rerank(new RerankRequest(string(input, "query"), documents(input), intValue(input, "topK", 1), Map.of()));
                case "resource_rerank" -> rerank.rerank(new RerankRequest(string(input, "query"), documents(input), intValue(input, "topK", 3), Map.of("source", "resource")));
                case "diagram_generate" -> Map.of(
                        "diagramType", string(input, "diagramType"),
                        "diagramScript", generateDiagramScript(input),
                        "renderHint", "Prefer compact flowchart labels and top-down layout."
                );
                case "mermaid_render" -> mermaid.render(new MermaidRenderRequest(string(input, "diagramScript"), string(input, "outputName"), string(input, "format")));
                case "diagram_render" -> mermaid.render(new MermaidRenderRequest(string(input, "diagramScript"), string(input, "outputName"), string(input, "format")));
                case "memory_read" -> Map.of("summary", "mock memory read");
                case "memory_write" -> knowledgeCache.writeCandidate(input);
                case "resource_bundle_write" -> knowledgeCache.writeCandidate(input);
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

    @SuppressWarnings("unchecked")
    private List<String> collections(Map<String, Object> input, String key) {
        Object value = input == null ? null : input.get(key);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private WebSearchResponse filterVideos(WebSearchResponse response, List<String> platforms) {
        if (response == null || response.results() == null) {
            return new WebSearchResponse(List.of(), false);
        }
        if (platforms == null || platforms.isEmpty()) {
            return response;
        }
        List<WebSearchResult> filtered = response.results().stream()
                .filter(item -> platforms.stream().anyMatch(platform -> item.url() != null && item.url().contains(platform)))
                .toList();
        return new WebSearchResponse(filtered, response.mock());
    }

    private String withSiteFilter(String query, List<String> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            return query;
        }
        String filters = platforms.stream().map(platform -> "site:" + platform).reduce((left, right) -> left + " OR " + right).orElse("");
        return filters.isBlank() ? query : filters + " " + query;
    }

    private String generateDiagramScript(Map<String, Object> input) {
        String diagramType = string(input, "diagramType");
        List<String> nodes = collections(input, "nodeSummary");
        if (nodes.isEmpty()) {
            nodes = List.of("读取问题", "识别知识点", "拆解步骤", "形成回答");
        }
        StringBuilder builder = new StringBuilder("flowchart TD").append(System.lineSeparator());
        for (int i = 0; i < nodes.size(); i++) {
            String nodeId = String.valueOf((char) ('A' + i));
            builder.append("    ").append(nodeId).append("[").append(nodes.get(i)).append("]").append(System.lineSeparator());
            if (i > 0) {
                String previous = String.valueOf((char) ('A' + i - 1));
                builder.append("    ").append(previous).append(" --> ").append(nodeId).append(System.lineSeparator());
            }
        }
        if (!diagramType.isBlank()) {
            builder.append("    %% diagramType=").append(diagramType).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
