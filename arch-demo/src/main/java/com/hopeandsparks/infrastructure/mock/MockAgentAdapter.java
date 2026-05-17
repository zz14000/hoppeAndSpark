package com.hopeandsparks.infrastructure.mock;


/**
 * 文件职责：MockAgentAdapter 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\infrastructure\mock\MockAgentAdapter.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.domain.agent.AgentConversation;
import com.hopeandsparks.domain.agent.AgentGateway;
import com.hopeandsparks.domain.agent.AgentMessageResult;
import com.hopeandsparks.domain.agent.AgentWorkflowResult;
import com.hopeandsparks.domain.agent.CreateConversationCommand;
import com.hopeandsparks.domain.agent.RunWorkflowCommand;
import com.hopeandsparks.domain.agent.SendAgentMessageCommand;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hope.agent", name = "mode", havingValue = "mock", matchIfMissing = true)
public class MockAgentAdapter implements AgentGateway {

    @Override
    public AgentConversation createConversation(CreateConversationCommand command) {
        String sessionId = "mock_session_" + UUID.randomUUID();
        return new AgentConversation(sessionId, command.agentCode(), "mock_coze_conversation_" + sessionId);
    }

    @Override
    public AgentMessageResult sendMessage(SendAgentMessageCommand command) {
        String agentCode = command.agentCode() == null || command.agentCode().isBlank() ? "sage" : command.agentCode();
        String content = "Mock " + agentCode + " received: " + command.content();
        return new AgentMessageResult(
            command.sessionId(),
            agentCode,
            "assistant",
            content,
            "mock_message_" + UUID.randomUUID(),
            Map.of("adapter", "mock", "stream", false)
        );
    }

    @Override
    public AgentWorkflowResult runWorkflow(RunWorkflowCommand command) {
        return new AgentWorkflowResult(
            command.workflowCode(),
            "mock_run_" + UUID.randomUUID(),
            "success",
            Map.of("adapter", "mock", "summary", "workflow completed")
        );
    }
}

