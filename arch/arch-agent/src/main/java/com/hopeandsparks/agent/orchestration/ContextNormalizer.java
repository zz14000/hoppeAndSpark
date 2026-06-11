package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.RetrievalBundle;
import com.hopeandsparks.agent.dto.HybridRetrievalRequest;
import com.hopeandsparks.agent.dto.HybridRetrievalResult;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.config.AgentProperties;
import com.hopeandsparks.agent.service.RetrievalOrchestrator;
import com.hopeandsparks.infra.chroma.RetrievedChunk;
import com.hopeandsparks.infra.rerank.RerankResult;
import com.hopeandsparks.infra.search.WebSearchResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ContextNormalizer {

    private final RetrievalOrchestrator retrievalOrchestrator;
    private final AgentProperties agentProperties;

    public ContextNormalizer(RetrievalOrchestrator retrievalOrchestrator, AgentProperties agentProperties) {
        this.retrievalOrchestrator = retrievalOrchestrator;
        this.agentProperties = agentProperties;
    }

    public RetrievalBundle ragContext(AgentRunRequest request) {
        HybridRetrievalResult result = retrievalOrchestrator.retrieve(new HybridRetrievalRequest(
                safe(request.userId(), "anonymous"),
                safe(request.projectId(), "default"),
                safe(request.userQuery(), ""),
                request.knowledgePointIds() == null ? List.of() : request.knowledgePointIds(),
                request.allowWebSearch() || agentProperties.getWebSearch().isDefaultAllowed(),
                request.retrievalMode() == null ? com.hopeandsparks.agent.enums.AgentRetrievalMode.KB_FIRST_CONTROLLED_WEB : request.retrievalMode(),
                request.maxContextChunks() <= 0 ? agentProperties.getRetrieval().getDefaultTopK() : request.maxContextChunks()
        ));
        List<RetrievedChunk> chunks = result.kbVectorHits().stream()
                .map(hit -> new RetrievedChunk(hit.chunkId(), hit.text(), hit.title(), hit.url(), hit.score(), hit.metadata()))
                .toList();
        List<WebSearchResult> webResults = result.fusedHits().stream()
                .filter(hit -> "web".equals(hit.sourceType()))
                .map(hit -> new WebSearchResult(hit.title(), hit.url(), hit.text(), java.time.LocalDateTime.now(), hit.score()))
                .toList();
        List<RerankResult> reranked = result.fusedHits().stream()
                .limit(Math.max(1, request.maxContextChunks() <= 0 ? agentProperties.getRetrieval().getDefaultTopK() : request.maxContextChunks()))
                .map(hit -> new RerankResult(hit.rank(), hit.text(), hit.score()))
                .toList();
        return new RetrievalBundle(
                chunks,
                result.kbVectorHits(),
                result.kbKeywordHits(),
                result.candidateHits(),
                webResults,
                result.candidateIds(),
                result.fusedHits(),
                result.plan(),
                reranked,
                result.citations(),
                result.webSearchUsed(),
                result.qualityFlags(),
                Map.of(
                        "rewrittenQueries", result.plan().rewrittenQueries(),
                        "fusedCount", result.fusedHits().size()
                )
        );
    }

    private String safe(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

}
