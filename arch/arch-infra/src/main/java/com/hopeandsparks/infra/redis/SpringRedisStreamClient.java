package com.hopeandsparks.infra.redis;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class SpringRedisStreamClient implements RedisStreamClient {

    private final StringRedisTemplate redisTemplate;

    public SpringRedisStreamClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String publish(String streamKey, Map<String, String> body) {
        RecordId recordId = redisTemplate.opsForStream()
                .add(StreamRecords.newRecord().in(streamKey).ofMap(body == null ? Map.of() : body));
        return recordId == null ? "" : recordId.getValue();
    }

    @Override
    public void ensureGroup(String streamKey, String group) {
        if (group == null || group.isBlank()) {
            return;
        }
        try {
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.latest(), group);
        } catch (RuntimeException exception) {
            String message = String.valueOf(exception.getMessage()).toLowerCase();
            if (!message.contains("busygroup") && !message.contains("already exists")) {
                throw exception;
            }
        }
    }

    @Override
    public List<RedisStreamMessage> readGroup(String streamKey, String group, String consumer, int count, long blockMs) {
        ensureGroup(streamKey, group);
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                Consumer.from(group, blankToDefault(consumer, "default-consumer")),
                StreamReadOptions.empty()
                        .count(Math.max(1, count))
                        .block(Duration.ofMillis(Math.max(0L, blockMs))),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed())
        );
        return toMessages(records);
    }

    @Override
    public List<RedisStreamMessage> readPending(String streamKey, String group, String consumer, long minIdleMs, int count) {
        ensureGroup(streamKey, group);
        PendingMessages summary = redisTemplate.opsForStream()
                .pending(streamKey, Consumer.from(group, blankToDefault(consumer, "default-consumer")));
        if (summary == null || summary.isEmpty()) {
            return List.of();
        }
        List<String> messageIds = summary.stream()
                .filter(message -> message.getElapsedTimeSinceLastDelivery().toMillis() >= Math.max(0L, minIdleMs))
                .limit(Math.max(1, count))
                .map(message -> message.getIdAsString())
                .toList();
        if (messageIds.isEmpty()) {
            return List.of();
        }
        return claimPending(streamKey, group, consumer, minIdleMs, messageIds);
    }

    @Override
    public List<RedisStreamMessage> claimPending(String streamKey, String group, String consumer, long minIdleMs, List<String> messageIds) {
        ensureGroup(streamKey, group);
        if (messageIds == null || messageIds.isEmpty()) {
            return List.of();
        }
        List<RecordId> recordIds = messageIds.stream().map(RecordId::of).toList();
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().claim(
                streamKey,
                group,
                blankToDefault(consumer, "default-consumer"),
                Duration.ofMillis(Math.max(0L, minIdleMs)),
                recordIds.toArray(RecordId[]::new)
        );
        return toMessages(records);
    }

    @Override
    public List<RedisStreamMessage> list(String streamKey) {
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                .range(streamKey, Range.unbounded());
        return toMessages(records);
    }

    @Override
    public void ack(String streamKey, String group, String messageId) {
        if (group == null || group.isBlank() || messageId == null || messageId.isBlank()) {
            return;
        }
        redisTemplate.opsForStream().acknowledge(streamKey, group, RecordId.of(messageId));
    }

    private LocalDateTime resolveCreatedAt(MapRecord<String, Object, Object> record) {
        String rawId = record.getId() == null ? "" : record.getId().getValue();
        String[] segments = rawId.split("-", 2);
        if (segments.length > 0) {
            try {
                long timestampMs = Long.parseLong(segments[0]);
                return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestampMs), ZoneId.systemDefault());
            } catch (NumberFormatException ignored) {
                // fall through to now
            }
        }
        return LocalDateTime.now();
    }

    private List<RedisStreamMessage> toMessages(List<MapRecord<String, Object, Object>> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        return records.stream()
                .map(record -> new RedisStreamMessage(
                        record.getId().getValue(),
                        String.valueOf(record.getStream()),
                        record.getValue().entrySet().stream().collect(java.util.stream.Collectors.toMap(
                                entry -> String.valueOf(entry.getKey()),
                                entry -> String.valueOf(entry.getValue())
                        )),
                        resolveCreatedAt(record)
                ))
                .toList();
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
