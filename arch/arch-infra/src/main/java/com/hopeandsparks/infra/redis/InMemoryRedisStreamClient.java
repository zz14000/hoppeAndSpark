package com.hopeandsparks.infra.redis;

import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.common.web.RequestContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis Stream 的内存 mock 实现。
 * W1 不要求真实 Redis Stream，先让调用方能发布和查看消息。
 */
@Service
public class InMemoryRedisStreamClient implements RedisStreamClient {

    private final Map<String, List<RedisStreamMessage>> streams = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public synchronized String publish(String streamKey, Map<String, String> body) {
        if (streamKey == null || streamKey.isBlank()) {
            throw new BusinessException(400, "streamKey 不能为空");
        }
        Map<String, String> copiedBody = new LinkedHashMap<>();
        if (body != null) {
            copiedBody.putAll(body);
        }
        copiedBody.putIfAbsent("requestId", RequestContext.getRequestId());

        String messageId = System.currentTimeMillis() + "-" + sequence.getAndIncrement();
        RedisStreamMessage message = new RedisStreamMessage(
                messageId,
                streamKey,
                copiedBody,
                copiedBody.get("requestId"),
                LocalDateTime.now()
        );
        streams.computeIfAbsent(streamKey, key -> new ArrayList<>()).add(message);
        return messageId;
    }

    @Override
    public synchronized List<RedisStreamMessage> list(String streamKey) {
        return List.copyOf(streams.getOrDefault(streamKey, List.of()));
    }

    @Override
    public void ack(String streamKey, String group, String messageId) {
        // mock 模式不维护消费组状态，先保留方法让业务代码可以调用。
    }
}
