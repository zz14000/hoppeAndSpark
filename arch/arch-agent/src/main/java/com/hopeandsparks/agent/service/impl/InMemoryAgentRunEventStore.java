package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.AgentStageEvent;
import com.hopeandsparks.agent.service.AgentRunEventStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryAgentRunEventStore implements AgentRunEventStore {

    private final Map<String, List<AgentStageEvent>> store = new ConcurrentHashMap<>();

    @Override
    public void save(String runId, List<AgentStageEvent> events) {
        store.put(runId, List.copyOf(events));
    }

    @Override
    public List<AgentStageEvent> list(String runId) {
        return store.getOrDefault(runId, List.of());
    }
}
