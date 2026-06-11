package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.AgentStageEvent;
import com.hopeandsparks.agent.repository.AgentRunEventRecord;

import java.util.List;

public interface AgentRunEventStore {

    void save(String runId, List<AgentStageEvent> events);

    List<AgentStageEvent> list(String runId);

    List<AgentRunEventRecord> listRecords(String runId);
}
