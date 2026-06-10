package com.hopeandsparks.infra.redis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockRedisStreamClient implements RedisStreamClient {

    private final Map<String, List<RedisStreamMessage>> streams = new ConcurrentHashMap<>();

    @Override
    public String publish(String streamKey, Map<String, String> body) {
        String messageId = System.currentTimeMillis() + "-0";
        streams.computeIfAbsent(streamKey, key -> new ArrayList<>())
                .add(new RedisStreamMessage(messageId, streamKey, body, LocalDateTime.now()));
        return messageId;
    }

    @Override
    public List<RedisStreamMessage> list(String streamKey) {
        return List.copyOf(streams.getOrDefault(streamKey, List.of()));
    }

    @Override
    public void ack(String streamKey, String group, String messageId) {
        // Mock client keeps messages visible for debugging.
    }
}
