package com.hopeandsparks.agent.dto;

import com.hopeandsparks.infra.chroma.RetrievedChunk;
import com.hopeandsparks.infra.rerank.RerankResult;
import com.hopeandsparks.infra.search.WebSearchResult;

import java.util.List;

public record RetrievalBundle(
        List<RetrievedChunk> kbChunks,
        List<WebSearchResult> webResults,
        List<String> candidateIds,
        List<RerankResult> reranked,
        List<String> citations,
        boolean webSearchUsed,
        List<String> retrievalQualityFlags
) {
}
