package com.hopeandsparks.domain.agent;


/**
 * 文件职责：AgentWorkflowResult 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\agent\AgentWorkflowResult.java，用于承载对应分层或接口的基础职责。
 */
import java.util.Map;

public record AgentWorkflowResult(
    String workflowCode,
    String externalRunId,
    String status,
    Map<String, Object> output
) {
}

