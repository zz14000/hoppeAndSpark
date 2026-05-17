package com.hopeandsparks.application.agent;


/**
 * 文件职责：AgentChatService 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\application\agent\AgentChatService.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.domain.agent.AgentGateway;
import com.hopeandsparks.domain.agent.AgentMessageResult;
import com.hopeandsparks.domain.agent.SendAgentMessageCommand;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AgentChatService {

    private final AgentGateway agentGateway;

    public AgentChatService(AgentGateway agentGateway) {
        this.agentGateway = agentGateway;
    }

    public AgentMessageResult sendMessage(String sessionId, String agentCode, String userId, String content) {
        return agentGateway.sendMessage(new SendAgentMessageCommand(
            sessionId,
            agentCode,
            userId,
            content,
            Map.of("source", "api")
        ));
    }
}

