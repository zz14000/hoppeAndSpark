package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.AgentStageEvent;
import com.hopeandsparks.agent.repository.AgentRunEventRecord;
import com.hopeandsparks.agent.repository.AgentRunEventRepository;
import com.hopeandsparks.agent.service.AgentRunEventStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryAgentRunEventStore implements AgentRunEventStore {

    private final Map<String, List<AgentStageEvent>> store = new ConcurrentHashMap<>();
    private final AgentRunEventRepository repository;

    public InMemoryAgentRunEventStore(AgentRunEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(String runId, List<AgentStageEvent> events) {
        store.put(runId, List.copyOf(events));
        for (AgentStageEvent event : events) {
            repository.save(new AgentRunEventRecord(
                    null,
                    runId,
                    String.valueOf(event.payload().getOrDefault("nodeName", event.stage())),
                    event.stage(),
                    "SUCCESS",
                    event.summary(),
                    String.valueOf(event.payload()),
                    0L,
                    0,
                    java.time.LocalDateTime.now()
            ));
        }
    }

    @Override
    public List<AgentStageEvent> list(String runId) {
        return store.getOrDefault(runId, List.of());
    }

    @Override
    public List<AgentRunEventRecord> listRecords(String runId) {
        return repository.listByRunId(runId);
    }
}
