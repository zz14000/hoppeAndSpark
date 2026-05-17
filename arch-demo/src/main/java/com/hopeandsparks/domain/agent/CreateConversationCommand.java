package com.hopeandsparks.domain.agent;


/**
 * 文件职责：CreateConversationCommand 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\agent\CreateConversationCommand.java，用于承载对应分层或接口的基础职责。
 */
import java.util.Map;

public record CreateConversationCommand(
    String agentCode,
    String userId,
    Map<String, Object> context
) {
}

