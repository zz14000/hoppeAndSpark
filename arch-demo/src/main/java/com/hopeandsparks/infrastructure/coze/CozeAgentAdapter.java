package com.hopeandsparks.infrastructure.coze;


/**
 * 文件职责：CozeAgentAdapter 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\infrastructure\coze\CozeAgentAdapter.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.domain.agent.AgentConversation;
import com.hopeandsparks.domain.agent.AgentGateway;
import com.hopeandsparks.domain.agent.AgentMessageResult;
import com.hopeandsparks.domain.agent.AgentWorkflowResult;
import com.hopeandsparks.domain.agent.CreateConversationCommand;
import com.hopeandsparks.domain.agent.RunWorkflowCommand;
import com.hopeandsparks.domain.agent.SendAgentMessageCommand;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hope.agent", name = "mode", havingValue = "coze")
public class CozeAgentAdapter implements AgentGateway {

    @Override
    public AgentConversation createConversation(CreateConversationCommand command) {
        throw new UnsupportedOperationException("Coze adapter is a skeleton. Wire Coze API here.");
    }

    @Override
    public AgentMessageResult sendMessage(SendAgentMessageCommand command) {
        throw new UnsupportedOperationException("Coze adapter is a skeleton. Wire Coze API here.");
    }

    @Override
    public AgentWorkflowResult runWorkflow(RunWorkflowCommand command) {
        throw new UnsupportedOperationException("Coze adapter is a skeleton. Wire Coze API here.");
    }
}

