package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.ResourceSearchRequest;
import com.hopeandsparks.agent.dto.ResourceSearchResult;

public interface ResourceRetrievalOrchestrator {

    ResourceSearchResult retrieve(ResourceSearchRequest request);
}
