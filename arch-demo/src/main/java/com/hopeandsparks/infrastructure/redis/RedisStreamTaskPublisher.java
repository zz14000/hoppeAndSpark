package com.hopeandsparks.infrastructure.redis;

/**
 * 文件职责：把业务异步任务写入 Redis Stream，队列命名沿用数据库老版本设计中的 queue:* 规则。
 */
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeandsparks.domain.task.TaskMessage;
import com.hopeandsparks.domain.task.TaskPublisher;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "hope.queue", name = "mode", havingValue = "redis-stream")
public class RedisStreamTaskPublisher implements TaskPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisStreamTaskPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(TaskMessage message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("routingKey", safeValue(message.routingKey()));
        body.put("idempotentKey", safeValue(message.idempotentKey()));
        body.put("payload", toJson(message.payload()));
        body.put("createdAt", Instant.now().toString());

        redisTemplate.opsForStream().add(streamKey(message.routingKey()), body);
    }

    private String streamKey(String routingKey) {
        if (!StringUtils.hasText(routingKey)) {
            return "queue:default";
        }
        return switch (routingKey) {
            case "kb.document.parse", "kb.parse" -> "queue:kb:parse";
            case "kb.chunk.embed", "kb.embed" -> "queue:kb:embed";
            default -> "queue:" + routingKey.replace('.', ':');
        };
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Task payload cannot be serialized to Redis Stream.", exception);
        }
    }
}
