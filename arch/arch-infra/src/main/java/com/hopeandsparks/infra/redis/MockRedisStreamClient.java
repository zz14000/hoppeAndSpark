package com.hopeandsparks.infra.redis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockRedisStreamClient implements RedisStreamClient {

    private final Map<String, List<RedisStreamMessage>> streams = new ConcurrentHashMap<>();
    private final Map<String, Boolean> groups = new ConcurrentHashMap<>();

    @Override
    public String publish(String streamKey, Map<String, String> body) {
        String messageId = System.currentTimeMillis() + "-0";
        streams.computeIfAbsent(streamKey, key -> new ArrayList<>())
                .add(new RedisStreamMessage(messageId, streamKey, body, LocalDateTime.now()));
        return messageId;
    }

    @Override
    public void ensureGroup(String streamKey, String group) {
        groups.put(streamKey + ":" + group, Boolean.TRUE);
    }

    @Override
    public List<RedisStreamMessage> readGroup(String streamKey, String group, String consumer, int count, long blockMs) {
        ensureGroup(streamKey, group);
        return streams.getOrDefault(streamKey, List.of()).stream()
                .limit(Math.max(1, count))
                .toList();
    }

    @Override
    public List<RedisStreamMessage> readPending(String streamKey, String group, String consumer, long minIdleMs, int count) {
        ensureGroup(streamKey, group);
        return List.of();
    }

    @Override
    public List<RedisStreamMessage> claimPending(String streamKey, String group, String consumer, long minIdleMs, List<String> messageIds) {
        ensureGroup(streamKey, group);
        if (messageIds == null || messageIds.isEmpty()) {
            return List.of();
        }
        return streams.getOrDefault(streamKey, List.of()).stream()
                .filter(message -> messageIds.contains(message.messageId()))
                .toList();
    }

    @Override
    public List<RedisStreamMessage> list(String streamKey) {
        return List.copyOf(streams.getOrDefault(streamKey, List.of()));
    }

    @Override
    public void ack(String streamKey, String group, String messageId) {
        streams.computeIfAbsent(streamKey, key -> new ArrayList<>())
                .removeIf(message -> message.messageId().equals(messageId));
    }
}
