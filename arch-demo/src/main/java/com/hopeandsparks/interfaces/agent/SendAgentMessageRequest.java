package com.hopeandsparks.interfaces.agent;


/**
 * 文件职责：SendAgentMessageRequest 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\interfaces\agent\SendAgentMessageRequest.java，用于承载对应分层或接口的基础职责。
 */
import jakarta.validation.constraints.NotBlank;

public record SendAgentMessageRequest(
    String agentCode,
    String userId,
    @NotBlank String content
) {
}

