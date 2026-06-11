package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.HybridRetrievalRequest;
import com.hopeandsparks.agent.dto.HybridRetrievalResult;

public interface RetrievalOrchestrator {

    HybridRetrievalResult retrieve(HybridRetrievalRequest request);
}
