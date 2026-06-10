package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.RetrievalBundle;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.infra.chroma.RetrievedChunk;
import com.hopeandsparks.infra.chroma.VectorSearchResponse;
import com.hopeandsparks.infra.rerank.RerankResponse;
import com.hopeandsparks.infra.search.WebSearchResponse;
import com.hopeandsparks.infra.tool.ToolRegistry;
import com.hopeandsparks.agent.config.AgentProperties;
import com.hopeandsparks.agent.service.KnowledgeCacheService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ContextNormalizer {

    private final ToolRegistry toolRegistry;
    private final KnowledgeCacheService knowledgeCacheService;
    private final AgentProperties agentProperties;

    public ContextNormalizer(ToolRegistry toolRegistry, KnowledgeCacheService knowledgeCacheService, AgentProperties agentProperties) {
        this.toolRegistry = toolRegistry;
        this.knowledgeCacheService = knowledgeCacheService;
        this.agentProperties = agentProperties;
    }

    public RetrievalBundle ragContext(AgentRunRequest request) {
        VectorSearchResponse kb = (VectorSearchResponse) toolRegistry.call("kb_search", Map.of(
                "userId", safe(request.userId(), "anonymous"),
                "projectId", safe(request.projectId(), "default"),
                "collection", "edu_ground_truth",
                "query", safe(request.userQuery(), ""),
                "topK", 5
        ));
        boolean useWeb = request.allowWebSearch()
                || (agentProperties.getWebSearch().isDefaultAllowed() && kb.chunks().size() < 3);
        WebSearchResponse web = useWeb
                ? (WebSearchResponse) toolRegistry.call("web_search", Map.of(
                "query", safe(request.userQuery(), ""),
                "topK", 3
        ))
                : new WebSearchResponse(List.of(), false);
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
        List<String> candidateIds = useWeb
                ? knowledgeCacheService.cacheCandidates(safe(request.userId(), "anonymous"), safe(request.projectId(), "default"), safe(request.userQuery(), ""), web.results())
                : List.of();
        List<String> citations = new ArrayList<>();
        citations.addAll(kb.chunks().stream()
                .map(this::toCitation)
                .toList());
        citations.addAll(web.results().stream()
                .map(result -> result.title() + " - " + result.url())
                .toList());
        List<String> qualityFlags = new ArrayList<>();
        if (kb.chunks().isEmpty()) {
            qualityFlags.add("kb_empty");
        }
        if (useWeb) {
            qualityFlags.add("web_search_used");
        }
        if (citations.isEmpty()) {
            qualityFlags.add("citation_missing");
        }
        return new RetrievalBundle(
                kb.chunks(),
                web.results(),
                candidateIds,
                reranked.results(),
                citations,
                useWeb,
                qualityFlags
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
