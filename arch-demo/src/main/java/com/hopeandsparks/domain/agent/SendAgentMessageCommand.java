package com.hopeandsparks.domain.agent;


/**
 * 文件职责：SendAgentMessageCommand 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\agent\SendAgentMessageCommand.java，用于承载对应分层或接口的基础职责。
 */
import java.util.Map;

public record SendAgentMessageCommand(
    String sessionId,
    String agentCode,
    String userId,
    String content,
    Map<String, Object> metadata
) {
}

