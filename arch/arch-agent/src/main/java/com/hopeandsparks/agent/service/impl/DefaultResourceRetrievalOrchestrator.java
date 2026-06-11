package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.ResourceBundle;
import com.hopeandsparks.agent.dto.ResourceItem;
import com.hopeandsparks.agent.dto.ResourceSearchRequest;
import com.hopeandsparks.agent.dto.ResourceSearchResult;
import com.hopeandsparks.agent.dto.ResourceSelectionDecision;
import com.hopeandsparks.agent.service.KnowledgeCacheService;
import com.hopeandsparks.agent.service.ResourceRetrievalOrchestrator;
import com.hopeandsparks.infra.chroma.ChromaVectorStoreGateway;
import com.hopeandsparks.infra.chroma.VectorSearchRequest;
import com.hopeandsparks.infra.chroma.VectorSearchResponse;
import com.hopeandsparks.infra.search.WebSearchResponse;
import com.hopeandsparks.infra.search.WebSearchResult;
import com.hopeandsparks.infra.tool.ToolRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DefaultResourceRetrievalOrchestrator implements ResourceRetrievalOrchestrator {

    private final ChromaVectorStoreGateway chromaVectorStoreGateway;
    private final ToolRegistry toolRegistry;
    private final KnowledgeCacheService knowledgeCacheService;

    public DefaultResourceRetrievalOrchestrator(
            ChromaVectorStoreGateway chromaVectorStoreGateway,
            ToolRegistry toolRegistry,
            KnowledgeCacheService knowledgeCacheService
    ) {
        this.chromaVectorStoreGateway = chromaVectorStoreGateway;
        this.toolRegistry = toolRegistry;
        this.knowledgeCacheService = knowledgeCacheService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResourceSearchResult retrieve(ResourceSearchRequest request) {
        List<String> preferredTypes = request.preferredResourceTypes() == null || request.preferredResourceTypes().isEmpty()
                ? List.of("article", "video")
                : request.preferredResourceTypes();
        List<String> rewrittenQueries = buildQueries(request.query());
        VectorSearchResponse kbAnchorResponse = chromaVectorStoreGateway.search(new VectorSearchRequest(
                safe(request.userId()),
                safe(request.projectId()),
                "edu_ground_truth",
                List.of("edu_ground_truth"),
                safe(request.query()),
                List.of(),
                Math.max(1, request.topK()),
                Map.of()
        ));
        String kbAnchor = kbAnchorResponse.chunks().stream()
                .map(chunk -> chunk.sourceTitle() == null || chunk.sourceTitle().isBlank() ? chunk.text() : chunk.sourceTitle())
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .map(text -> text.length() > 80 ? text.substring(0, 80) : text)
                .orElse("");

        List<ResourceItem> referenceResources = new ArrayList<>();
        List<ResourceItem> videoResources = new ArrayList<>();
        List<ResourceItem> practiceResources = new ArrayList<>();
        List<WebSearchResult> externalResults = new ArrayList<>();

        if (preferredTypes.contains("article") || preferredTypes.contains("reference")) {
            WebSearchResponse response = (WebSearchResponse) toolRegistry.call("resource_search", Map.of(
                    "query", queryWithAnchor(rewrittenQueries.getFirst(), kbAnchor),
                    "topK", request.topK(),
                    "resourceType", "article"
            ));
            referenceResources.addAll(toResources(response.results(), "article", request.knowledgePointIds(), ""));
            externalResults.addAll(response.results());
        }

        if (preferredTypes.contains("video")) {
            WebSearchResponse response = (WebSearchResponse) toolRegistry.call("video_search", Map.of(
                    "query", queryWithAnchor(rewrittenQueries.get(1), kbAnchor),
                    "topK", request.topK(),
                    "platforms", List.of("bilibili.com")
            ));
            videoResources.addAll(toResources(response.results(), "video", request.knowledgePointIds(), ""));
            externalResults.addAll(response.results());
        }

        if (preferredTypes.contains("practice")) {
            WebSearchResponse response = (WebSearchResponse) toolRegistry.call("resource_search", Map.of(
                    "query", queryWithAnchor(rewrittenQueries.get(2) + " 练习题", kbAnchor),
                    "topK", request.topK(),
                    "resourceType", "practice"
            ));
            practiceResources.addAll(toResources(response.results(), "practice", request.knowledgePointIds(), "practice"));
            externalResults.addAll(response.results());
        }

        List<String> candidateIds = request.allowWebSearch() && !externalResults.isEmpty()
                ? knowledgeCacheService.cacheCandidates(request.userId(), request.projectId(), request.query(), externalResults)
                : List.of();
        toolRegistry.call("resource_rerank", Map.of(
                "query", request.query(),
                "documents", mergeResources(referenceResources, videoResources, practiceResources).stream().map(ResourceItem::summary).toList(),
                "topK", Math.max(1, request.topK())
        ));
        List<String> qualityFlags = new ArrayList<>();
        if (videoResources.isEmpty()) {
            qualityFlags.add("video_empty");
        }
        if (referenceResources.isEmpty()) {
            qualityFlags.add("reference_empty");
        }
        if (practiceResources.isEmpty() && preferredTypes.contains("practice")) {
            qualityFlags.add("practice_empty");
        }
        ResourceBundle bundle = new ResourceBundle(
                null,
                dedupe(videoResources),
                dedupe(referenceResources),
                dedupe(practiceResources),
                qualityFlags,
                selectionReason(preferredTypes, kbAnchor)
        );
        ResourceSelectionDecision decision = new ResourceSelectionDecision(
                "resource_bundle",
                selectionReason(preferredTypes, kbAnchor),
                preferredTypes,
                qualityFlags,
                Map.of(
                        "rewrittenQueries", rewrittenQueries,
                        "kbAnchor", kbAnchor,
                        "candidateIds", candidateIds
                )
        );
        return new ResourceSearchResult(
                bundle,
                decision,
                candidateIds,
                qualityFlags,
                Map.of(
                        "rewrittenQueries", rewrittenQueries,
                        "kbAnchor", kbAnchor,
                        "referenceCount", referenceResources.size(),
                        "videoCount", videoResources.size(),
                        "practiceCount", practiceResources.size()
                )
        );
    }

    private List<String> buildQueries(String query) {
        String safeQuery = safe(query);
        return List.of(
                safeQuery + " 讲解",
                "site:bilibili.com " + safeQuery + " 视频讲解",
                safeQuery + " 关键词"
        );
    }

    private List<ResourceItem> toResources(List<WebSearchResult> results, String type, List<String> knowledgePoints, String difficulty) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results.stream()
                .map(item -> new ResourceItem(
                        UUID.randomUUID().toString(),
                        type,
                        safe(item.title()),
                        safe(item.url()),
                        sourceOf(item.url()),
                        safe(item.summary()),
                        "",
                        0L,
                        difficulty,
                        knowledgePoints == null ? List.of() : knowledgePoints,
                        item.confidence(),
                        Map.of("fetchedAt", String.valueOf(item.fetchedAt()))
                ))
                .toList();
    }

    private List<ResourceItem> mergeResources(List<ResourceItem> references, List<ResourceItem> videos, List<ResourceItem> practices) {
        List<ResourceItem> merged = new ArrayList<>();
        merged.addAll(references);
        merged.addAll(videos);
        merged.addAll(practices);
        return merged;
    }

    private List<ResourceItem> dedupe(List<ResourceItem> items) {
        Map<String, ResourceItem> unique = new LinkedHashMap<>();
        for (ResourceItem item : items) {
            unique.putIfAbsent(item.type() + ":" + item.url(), item);
        }
        return List.copyOf(unique.values());
    }

    private String selectionReason(List<String> preferredTypes, String kbAnchor) {
        return "selected=" + new LinkedHashSet<>(preferredTypes) + (kbAnchor.isBlank() ? "" : ", anchored_by=" + kbAnchor);
    }

    private String queryWithAnchor(String query, String kbAnchor) {
        if (kbAnchor == null || kbAnchor.isBlank()) {
            return query;
        }
        return query + " " + kbAnchor;
    }

    private String sourceOf(String url) {
        if (url == null || url.isBlank()) {
            return "unknown";
        }
        if (url.contains("bilibili.com")) {
            return "bilibili";
        }
        return url.replaceFirst("https?://", "").split("/")[0];
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
