package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.AgentCheckpointSnapshot;
import com.hopeandsparks.agent.orchestration.AgentGraphState;

import java.util.List;
import java.util.Optional;

public interface AgentCheckpointStore {

    void saveRun(AgentGraphState state, String runtime, String currentNode, String status, String errorCode, String errorMessage);

    void saveCheckpoint(AgentGraphState state, String nodeName);

    Optional<AgentGraphState> loadLatestState(String runId);

    Optional<AgentGraphState> loadCheckpointState(String checkpointId);

    List<AgentCheckpointSnapshot> listCheckpoints(String runId);
}
