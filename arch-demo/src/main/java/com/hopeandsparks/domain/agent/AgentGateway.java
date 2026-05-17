package com.hopeandsparks.domain.agent;


/**
 * 文件职责：AgentGateway 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\agent\AgentGateway.java，用于承载对应分层或接口的基础职责。
 */
public interface AgentGateway {

    AgentConversation createConversation(CreateConversationCommand command);

    AgentMessageResult sendMessage(SendAgentMessageCommand command);

    AgentWorkflowResult runWorkflow(RunWorkflowCommand command);
}

