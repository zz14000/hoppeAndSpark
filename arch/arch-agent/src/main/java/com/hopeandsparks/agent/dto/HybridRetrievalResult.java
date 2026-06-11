package com.hopeandsparks.agent.dto;

import java.util.List;

public record HybridRetrievalResult(
        RetrievalPlan plan,
        List<RetrievalHit> kbVectorHits,
        List<RetrievalHit> kbKeywordHits,
        List<RetrievalHit> candidateHits,
        List<RetrievalHit> fusedHits,
        List<String> candidateIds,
        List<String> citations,
        boolean webSearchUsed,
        List<String> qualityFlags
) {
}
