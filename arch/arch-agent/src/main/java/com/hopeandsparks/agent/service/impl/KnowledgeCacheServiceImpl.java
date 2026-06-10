package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.service.KnowledgeCacheService;
import com.hopeandsparks.infra.rerank.RerankGateway;
import com.hopeandsparks.infra.rerank.RerankRequest;
import com.hopeandsparks.infra.rerank.RerankResponse;
import com.hopeandsparks.infra.rerank.RerankResult;
import com.hopeandsparks.infra.search.WebSearchResponse;
import com.hopeandsparks.infra.search.WebSearchResult;
import com.hopeandsparks.infra.tool.ToolRegistry;
import com.hopeandsparks.kb.repository.KbCandidateRecord;
import com.hopeandsparks.kb.service.KbCandidateGovernanceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class KnowledgeCacheServiceImpl implements KnowledgeCacheService {

    private final ToolRegistry toolRegistry;
    private final RerankGateway rerankGateway;
    private final KbCandidateGovernanceService kbCandidateGovernanceService;

    public KnowledgeCacheServiceImpl(
            ToolRegistry toolRegistry,
            RerankGateway rerankGateway,
            KbCandidateGovernanceService kbCandidateGovernanceService
    ) {
        this.toolRegistry = toolRegistry;
        this.rerankGateway = rerankGateway;
        this.kbCandidateGovernanceService = kbCandidateGovernanceService;
    }

    @Override
    public List<String> cacheCandidates(String userId, String projectId, String query) {
        WebSearchResponse response = (WebSearchResponse) toolRegistry.call("web_search", Map.of(
                "query", safe(query),
                "topK", 5
        ));
        return cacheCandidates(userId, projectId, query, response.results());
    }

    @Override
    public List<String> cacheCandidates(String userId, String projectId, String query, List<WebSearchResult> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        Map<String, Double> scores = rerank(query, results);
        List<KbCandidateRecord> candidates = kbCandidateGovernanceService.recordCandidates(
                safe(userId),
                safe(projectId),
                safe(query),
                results,
                scores
        );
        toolRegistry.call("memory_write", Map.of(
                "level", "kb_cache_candidate",
                "userId", safe(userId),
                "projectId", safe(projectId),
                "count", candidates.size(),
                "query", safe(query)
        ));
        return candidates.stream().map(KbCandidateRecord::candidateId).toList();
    }

    private Map<String, Double> rerank(String query, List<WebSearchResult> results) {
        if (results == null || results.isEmpty()) {
            return Map.of();
        }
        RerankResponse response = rerankGateway.rerank(new RerankRequest(
                safe(query),
                results.stream().map(item -> item.summary() == null ? "" : item.summary()).toList(),
                results.size(),
                Map.of("source", "web_cache")
        ));
        Map<String, Double> scores = new LinkedHashMap<>();
        for (RerankResult item : response.results()) {
            if (item.index() >= 0 && item.index() < results.size()) {
                scores.put(results.get(item.index()).url(), item.score());
            }
        }
        return scores;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "unknown" : value.replaceAll("\\s+", "_");
    }
}
