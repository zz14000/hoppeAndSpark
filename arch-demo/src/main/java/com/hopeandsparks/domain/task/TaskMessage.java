package com.hopeandsparks.domain.task;


/**
 * 文件职责：TaskMessage 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\task\TaskMessage.java，用于承载对应分层或接口的基础职责。
 */
import java.util.Map;

public record TaskMessage(
    String routingKey,
    String idempotentKey,
    Map<String, Object> payload
) {
}

