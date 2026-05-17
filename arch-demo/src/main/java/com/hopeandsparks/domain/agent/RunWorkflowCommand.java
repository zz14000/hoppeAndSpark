package com.hopeandsparks.domain.agent;


/**
 * 文件职责：RunWorkflowCommand 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\agent\RunWorkflowCommand.java，用于承载对应分层或接口的基础职责。
 */
import java.util.Map;

public record RunWorkflowCommand(
    String workflowCode,
    String userId,
    Map<String, Object> input
) {
}

