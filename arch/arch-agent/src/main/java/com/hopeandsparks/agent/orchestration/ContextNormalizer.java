package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.infra.chroma.RetrievedChunk;
import com.hopeandsparks.infra.chroma.VectorSearchResponse;
import com.hopeandsparks.infra.rerank.RerankResponse;
import com.hopeandsparks.infra.search.WebSearchResponse;
import com.hopeandsparks.infra.tool.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ContextNormalizer {

    private final ToolRegistry toolRegistry;

    public ContextNormalizer(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public Map<String, Object> ragContext(AgentRunRequest request) {
        VectorSearchResponse kb = (VectorSearchResponse) toolRegistry.call("kb_search", Map.of(
                "userId", safe(request.userId(), "anonymous"),
                "projectId", safe(request.projectId(), "default"),
                "collection", "edu_ground_truth",
                "query", safe(request.userQuery(), ""),
                "topK", 5
        ));
        WebSearchResponse web = (WebSearchResponse) toolRegistry.call("web_search", Map.of(
                "query", safe(request.userQuery(), ""),
                "topK", 3
        ));
        List<String> documents = new ArrayList<>();
        documents.addAll(kb.chunks().stream()
                .map(chunk -> chunk.sourceTitle() + "\n" + chunk.text() + "\nURL: " + chunk.sourceUrl())
                .filter(text -> !text.isBlank())
                .toList());
        documents.addAll(web.results().stream()
                .map(result -> result.title() + "\n" + result.summary() + "\nURL: " + result.url())
                .filter(text -> !text.isBlank())
                .toList());
        RerankResponse reranked = (RerankResponse) toolRegistry.call("rerank", Map.of(
                "query", safe(request.userQuery(), ""),
                "documents", documents,
                "topK", 5
        ));
        List<String> citations = new ArrayList<>();
        citations.addAll(kb.chunks().stream()
                .map(this::toCitation)
                .toList());
        citations.addAll(web.results().stream()
                .map(result -> result.title() + " - " + result.url())
                .toList());
        return Map.of(
                "kb", kb.chunks(),
                "reranked", reranked.results(),
                "web", web.results(),
                "citations", citations
        );
    }

    public Map<String, Object> memoryContext(AgentRunRequest request) {
        return Map.of(
                "l1", "mock recent summary for " + request.sessionId(),
                "l2", "mock project memory for " + request.projectId(),
                "l3", "mock user profile for " + request.userId()
        );
    }

    private String safe(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String toCitation(RetrievedChunk chunk) {
        String title = safe(chunk.sourceTitle(), "Knowledge Base");
        String url = safe(chunk.sourceUrl(), "kb://" + safe(chunk.chunkId(), "unknown"));
        return title + " - " + url;
    }
}
