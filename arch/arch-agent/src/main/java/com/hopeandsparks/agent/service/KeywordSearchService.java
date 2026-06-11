package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.RetrievalHit;

import java.util.List;

public interface KeywordSearchService {

    List<RetrievalHit> searchFormal(String userId, String projectId, String query, int topK);

    List<RetrievalHit> searchCandidates(String userId, String projectId, String query, int topK);
}
