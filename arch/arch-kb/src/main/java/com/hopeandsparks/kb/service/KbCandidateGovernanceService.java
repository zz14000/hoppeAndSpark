package com.hopeandsparks.kb.service;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.search.WebSearchResult;
import com.hopeandsparks.kb.repository.KbCandidateRecord;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface KbCandidateGovernanceService {

    GovernanceResult governDocument(String documentId, String userId, String projectId, String collection);

    List<KbCandidateRecord> recordCandidates(String userId, String projectId, String query, List<WebSearchResult> results, Map<String, Double> rerankScores);

    PageResponse<KbCandidateRecord> listCandidates(Map<String, String> query);

    Optional<KbCandidateRecord> findCandidate(String candidateId);

    KbCandidateRecord approve(String candidateId, String reviewerId, String comment);

    KbCandidateRecord reject(String candidateId, String reviewerId, String comment);

    KbCandidateRecord rollback(String candidateId, String reviewerId, String comment);

    KbCandidateRecord replay(String candidateId, String reviewerId);
}
