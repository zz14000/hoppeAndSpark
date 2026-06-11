package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.HybridRetrievalRequest;
import com.hopeandsparks.agent.dto.HybridRetrievalResult;
import com.hopeandsparks.agent.dto.RetrievalHit;
import com.hopeandsparks.agent.dto.RetrievalPlan;
import com.hopeandsparks.agent.enums.AgentRetrievalMode;
import com.hopeandsparks.agent.service.KeywordSearchService;
import com.hopeandsparks.agent.service.KnowledgeCacheService;
import com.hopeandsparks.agent.service.RetrievalOrchestrator;
import com.hopeandsparks.infra.chroma.ChromaVectorStoreGateway;
import com.hopeandsparks.infra.chroma.RetrievedChunk;
import com.hopeandsparks.infra.chroma.VectorSearchRequest;
import com.hopeandsparks.infra.chroma.VectorSearchResponse;
import com.hopeandsparks.infra.rerank.RerankGateway;
import com.hopeandsparks.infra.rerank.RerankRequest;
import com.hopeandsparks.infra.rerank.RerankResponse;
import com.hopeandsparks.infra.rerank.RerankResult;
import com.hopeandsparks.infra.search.WebSearchGateway;
import com.hopeandsparks.infra.search.WebSearchRequest;
import com.hopeandsparks.infra.search.WebSearchResponse;
import com.hopeandsparks.infra.search.WebSearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class HybridRetrievalOrchestrator implements RetrievalOrchestrator {

    private final ChromaVectorStoreGateway chromaVectorStoreGateway;
    private final KeywordSearchService keywordSearchService;
    private final WebSearchGateway webSearchGateway;
    private final RerankGateway rerankGateway;
    private final KnowledgeCacheService knowledgeCacheService;

    public HybridRetrievalOrchestrator(
            ChromaVectorStoreGateway chromaVectorStoreGateway,
            KeywordSearchService keywordSearchService,
            WebSearchGateway webSearchGateway,
            RerankGateway rerankGateway,
            KnowledgeCacheService knowledgeCacheService
    ) {
        this.chromaVectorStoreGateway = chromaVectorStoreGateway;
        this.keywordSearchService = keywordSearchService;
        this.webSearchGateway = webSearchGateway;
        this.rerankGateway = rerankGateway;
        this.knowledgeCacheService = knowledgeCacheService;
    }

    @Override
    public HybridRetrievalResult retrieve(HybridRetrievalRequest request) {
        List<String> rewrittenQueries = rewriteQueries(request.query());
        RetrievalPlan plan = new RetrievalPlan(
                safe(request.query()),
                rewrittenQueries,
                webAllowed(request),
                false,
                Math.max(3, request.topK())
        );
        List<RetrievalHit> vectorHits = new ArrayList<>();
        List<RetrievalHit> keywordHits = new ArrayList<>();
        List<RetrievalHit> candidateHits = new ArrayList<>();
        AtomicInteger rank = new AtomicInteger(1);
        for (String query : rewrittenQueries) {
            VectorSearchResponse formal = chromaVectorStoreGateway.search(new VectorSearchRequest(
                    safe(request.userId()),
                    safe(request.projectId()),
                    "edu_ground_truth",
                    List.of("edu_ground_truth"),
                    query,
                    List.of(),
                    request.topK(),
                    Map.of()
            ));
            vectorHits.addAll(formal.chunks().stream().map(chunk -> toHit(chunk, "vector", "edu_ground_truth", rank.getAndIncrement())).toList());
            keywordHits.addAll(keywordSearchService.searchFormal(request.userId(), request.projectId(), query, request.topK()));
            candidateHits.addAll(keywordSearchService.searchCandidates(request.userId(), request.projectId(), query, request.topK()));
        }
        boolean useWeb = shouldUseWeb(request, vectorHits, keywordHits);
        WebSearchResponse webResponse = useWeb
                ? webSearchGateway.search(new WebSearchRequest(safe(request.query()), request.topK(), Map.of("source", "agent_hybrid")))
                : new WebSearchResponse(List.of(), false);
        List<String> candidateIds = useWeb
                ? knowledgeCacheService.cacheCandidates(request.userId(), request.projectId(), request.query(), webResponse.results())
                : List.of();
        List<RetrievalHit> webHits = webResponse.results().stream()
                .map(result -> new RetrievalHit(
                        "web",
                        "web_search",
                        "",
                        result.url(),
                        result.title(),
                        result.url(),
                        result.summary(),
                        result.confidence(),
                        0,
                        Map.of("fetchedAt", String.valueOf(result.fetchedAt()))
                ))
                .toList();
        List<RetrievalHit> fusedHits = rrfFuse(vectorHits, keywordHits, candidateHits, webHits, request.topK());
        RerankResponse rerankResponse = rerankGateway.rerank(new RerankRequest(
                safe(request.query()),
                fusedHits.stream().map(RetrievalHit::text).toList(),
                Math.max(1, Math.min(fusedHits.size(), request.topK())),
                Map.of("source", "agent_hybrid")
        ));
        List<String> citations = fusedHits.stream()
                .limit(request.topK())
                .map(hit -> hit.title() + " - " + safe(hit.url()))
                .toList();
        List<String> qualityFlags = new ArrayList<>();
        if (vectorHits.isEmpty()) {
            qualityFlags.add("kb_empty");
        }
        if (keywordHits.isEmpty()) {
            qualityFlags.add("keyword_empty");
        }
        if (!candidateHits.isEmpty()) {
            qualityFlags.add("candidate_used");
        }
        if (useWeb) {
            qualityFlags.add("web_search_used");
        }
        if (citations.isEmpty()) {
            qualityFlags.add("citation_missing");
        }
        if (fusedHits.size() < 2) {
            qualityFlags.add("retrieval_low_confidence");
        }
        return new HybridRetrievalResult(
                new RetrievalPlan(plan.originalQuery(), plan.rewrittenQueries(), plan.webFallbackAllowed(), useWeb, plan.maxContextChunks()),
                dedupe(vectorHits),
                dedupe(keywordHits),
                dedupe(candidateHits),
                fusedHits,
                candidateIds,
                citations,
                useWeb,
                qualityFlags
        );
    }

    private List<String> rewriteQueries(String query) {
        if (query == null || query.isBlank()) {
            return List.of("");
        }
        return List.of(
                query,
                query + " 概念",
                query + " 步骤",
                query + " 关键词"
        );
    }

    private boolean shouldUseWeb(HybridRetrievalRequest request, List<RetrievalHit> vectorHits, List<RetrievalHit> keywordHits) {
        if (!webAllowed(request)) {
            return false;
        }
        return vectorHits.size() + keywordHits.size() < Math.max(3, request.topK());
    }

    private boolean webAllowed(HybridRetrievalRequest request) {
        return request.allowWebSearch() && request.retrievalMode() != AgentRetrievalMode.KB_ONLY;
    }

    private RetrievalHit toHit(RetrievedChunk chunk, String sourceType, String collection, int rank) {
        String documentId = String.valueOf(chunk.metadata().getOrDefault("documentId", ""));
        return new RetrievalHit(
                sourceType,
                collection,
                documentId,
                safe(chunk.chunkId()),
                safe(chunk.sourceTitle()),
                safe(chunk.sourceUrl()),
                safe(chunk.text()),
                chunk.score(),
                rank,
                chunk.metadata()
        );
    }

    private List<RetrievalHit> rrfFuse(
            List<RetrievalHit> vectorHits,
            List<RetrievalHit> keywordHits,
            List<RetrievalHit> candidateHits,
            List<RetrievalHit> webHits,
            int topK
    ) {
        Map<String, Double> scores = new LinkedHashMap<>();
        Map<String, RetrievalHit> index = new LinkedHashMap<>();
        addRrf(scores, index, vectorHits);
        addRrf(scores, index, keywordHits);
        addRrf(scores, index, candidateHits);
        addRrf(scores, index, webHits);
        return scores.entrySet().stream()
                .sorted((left, right) -> Double.compare(right.getValue(), left.getValue()))
                .limit(Math.max(1, topK))
                .map(entry -> {
                    RetrievalHit hit = index.get(entry.getKey());
                    return new RetrievalHit(
                            hit.sourceType(),
                            hit.collection(),
                            hit.documentId(),
                            hit.chunkId(),
                            hit.title(),
                            hit.url(),
                            hit.text(),
                            entry.getValue(),
                            hit.rank(),
                            hit.metadata()
                    );
                })
                .toList();
    }

    private void addRrf(Map<String, Double> scores, Map<String, RetrievalHit> index, List<RetrievalHit> hits) {
        int k = 60;
        for (int i = 0; i < hits.size(); i++) {
            RetrievalHit hit = hits.get(i);
            String key = hit.collection() + ":" + hit.chunkId();
            scores.merge(key, 1D / (k + i + 1), Double::sum);
            index.putIfAbsent(key, hit);
        }
    }

    private List<RetrievalHit> dedupe(List<RetrievalHit> hits) {
        Map<String, RetrievalHit> deduped = new LinkedHashMap<>();
        for (RetrievalHit hit : hits) {
            deduped.putIfAbsent(hit.collection() + ":" + hit.chunkId(), hit);
        }
        return List.copyOf(deduped.values());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
