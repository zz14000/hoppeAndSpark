package com.hopeandsparks.infra.redis;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    public List<RedisStreamMessage> list(String streamKey) {
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                .range(streamKey, Range.unbounded());
        return records.stream()
                .map(record -> new RedisStreamMessage(
                        record.getId().getValue(),
                        streamKey,
                        record.getValue().entrySet().stream().collect(java.util.stream.Collectors.toMap(
                                entry -> String.valueOf(entry.getKey()),
                                entry -> String.valueOf(entry.getValue())
                        )),
                        resolveCreatedAt(record)
                ))
                .toList();
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
}
