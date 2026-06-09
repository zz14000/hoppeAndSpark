package com.hopeandsparks.infra.redis;

import java.util.List;
import java.util.Map;

/**
 * Redis Stream 基础客户端。
 * 这里只定义发布、查看和确认三个简单动作，业务含义交给各业务模块处理。
 */
public interface RedisStreamClient {

    String publish(String streamKey, Map<String, String> body);

    List<RedisStreamMessage> list(String streamKey);

    void ack(String streamKey, String group, String messageId);
}
