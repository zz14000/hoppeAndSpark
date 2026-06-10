package com.hopeandsparks.agent.service;

import java.util.List;

public interface KnowledgeCacheService {

    List<String> cacheCandidates(String userId, String projectId, String query);

    List<String> cacheCandidates(String userId, String projectId, String query, java.util.List<com.hopeandsparks.infra.search.WebSearchResult> results);
}
