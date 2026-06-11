package com.hopeandsparks.agent.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeandsparks.agent.dto.AgentCheckpointSnapshot;
import com.hopeandsparks.agent.enums.AgentRunStatus;
import com.hopeandsparks.agent.orchestration.AgentGraphState;
import com.hopeandsparks.agent.repository.AgentCheckpointRepository;
import com.hopeandsparks.agent.repository.AgentRunCheckpointRecord;
import com.hopeandsparks.agent.repository.AgentRunRecord;
import com.hopeandsparks.agent.repository.AgentRunRepository;
import com.hopeandsparks.agent.service.AgentCheckpointStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DatabaseAgentCheckpointStore implements AgentCheckpointStore {

    private static final String RUN_POINTER_PREFIX = "agent:run:pointer:";

    private final AgentRunRepository runRepository;
    private final AgentCheckpointRepository checkpointRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final java.util.Map<String, String> fallbackPointers = new ConcurrentHashMap<>();

    public DatabaseAgentCheckpointStore(
            AgentRunRepository runRepository,
            AgentCheckpointRepository checkpointRepository,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.runRepository = runRepository;
        this.checkpointRepository = checkpointRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveRun(AgentGraphState state, String runtime, String currentNode, String status, String errorCode, String errorMessage) {
        runRepository.save(new AgentRunRecord(
                state.request().requestId(),
                safe(state.request().sessionId()),
                safe(state.request().userId()),
                safe(state.request().projectId()),
                toJson(state.request()),
                safe(runtime),
                safe(status),
                safe(currentNode),
                state.currentRevisionCount(),
                state.maxRevisionCount(),
                safe(errorCode),
                safe(errorMessage),
                resolveStartedAt(state, status),
                resolveFinishedAt(status)
        ));
    }

    @Override
    public void saveCheckpoint(AgentGraphState state, String nodeName) {
        String checkpointId = UUID.randomUUID().toString();
        checkpointRepository.save(new AgentRunCheckpointRecord(
                checkpointId,
                state.request().requestId(),
                nodeName,
                state.stateVersion(),
                toJson(state),
                toJson(state.payload()),
                LocalDateTime.now()
        ));
        rememberPointer(state.request().requestId(), checkpointId);
    }

    @Override
    public Optional<AgentGraphState> loadLatestState(String runId) {
        String checkpointId = latestPointer(runId).orElseGet(() -> checkpointRepository.findLatest(runId).map(AgentRunCheckpointRecord::checkpointId).orElse(""));
        if (checkpointId.isBlank()) {
            return Optional.empty();
        }
        return loadCheckpointState(checkpointId);
    }

    @Override
    public Optional<AgentGraphState> loadCheckpointState(String checkpointId) {
        return checkpointRepository.findByCheckpointId(checkpointId)
                .flatMap(record -> fromJson(record.checkpointStateJson()));
    }

    @Override
    public List<AgentCheckpointSnapshot> listCheckpoints(String runId) {
        return checkpointRepository.listByRunId(runId).stream()
                .map(record -> new AgentCheckpointSnapshot(
                        record.checkpointId(),
                        record.runId(),
                        record.nodeName(),
                        record.stateVersion(),
                        record.checkpointStateJson(),
                        java.util.Map.of("payloadJson", safe(record.payloadJson()))
                ))
                .toList();
    }

    private Optional<String> latestPointer(String runId) {
        String key = RUN_POINTER_PREFIX + safe(runId);
        if (fallbackPointers.containsKey(key)) {
            return Optional.ofNullable(fallbackPointers.get(key));
        }
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private void rememberPointer(String runId, String checkpointId) {
        String key = RUN_POINTER_PREFIX + safe(runId);
        fallbackPointers.put(key, checkpointId);
        try {
            redisTemplate.opsForValue().set(key, checkpointId, Duration.ofHours(6));
        } catch (RuntimeException exception) {
            // local fallback only
        }
    }

    private Optional<AgentGraphState> fromJson(String json) {
        try {
            return Optional.ofNullable(objectMapper.readValue(json, AgentGraphState.class));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private LocalDateTime resolveStartedAt(AgentGraphState state, String status) {
        return AgentRunStatus.PENDING.name().equalsIgnoreCase(status) ? null : LocalDateTime.now();
    }

    private LocalDateTime resolveFinishedAt(String status) {
        if (AgentRunStatus.SUCCESS.name().equalsIgnoreCase(status)
                || AgentRunStatus.FAILED.name().equalsIgnoreCase(status)
                || AgentRunStatus.BLOCKED.name().equalsIgnoreCase(status)
                || AgentRunStatus.INTERRUPTED.name().equalsIgnoreCase(status)) {
            return LocalDateTime.now();
        }
        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
