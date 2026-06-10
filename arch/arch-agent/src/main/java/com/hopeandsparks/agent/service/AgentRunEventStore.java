package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.AgentStageEvent;

import java.util.List;

public interface AgentRunEventStore {

    void save(String runId, List<AgentStageEvent> events);

    List<AgentStageEvent> list(String runId);
}
