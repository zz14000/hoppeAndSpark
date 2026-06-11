package com.hopeandsparks.agent.dto;

import com.hopeandsparks.infra.chroma.RetrievedChunk;
import com.hopeandsparks.infra.rerank.RerankResult;
import com.hopeandsparks.infra.search.WebSearchResult;

import java.util.List;
import java.util.Map;

public record RetrievalBundle(
        List<RetrievedChunk> kbChunks,
        List<RetrievalHit> kbVectorHits,
        List<RetrievalHit> kbKeywordHits,
        List<RetrievalHit> candidateHits,
        List<WebSearchResult> webResults,
        List<String> candidateIds,
        List<RetrievalHit> fusedHits,
        RetrievalPlan retrievalPlan,
        List<RerankResult> reranked,
        List<String> citations,
        boolean webSearchUsed,
        List<String> retrievalQualityFlags,
        Map<String, Object> retrievalDebug
) {
}
