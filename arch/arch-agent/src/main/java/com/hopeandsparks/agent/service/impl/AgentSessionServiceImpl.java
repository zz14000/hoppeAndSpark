package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.AgentMessageSendRequest;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentSessionCreateRequest;
import com.hopeandsparks.agent.service.AgentOrchestrationService;
import com.hopeandsparks.agent.service.AgentSessionService;
import com.hopeandsparks.agent.vo.AgentMessageSendVO;
import com.hopeandsparks.agent.vo.AgentMessageVO;
import com.hopeandsparks.agent.vo.AgentRunResultVO;
import com.hopeandsparks.agent.vo.AgentSessionVO;
import com.hopeandsparks.agent.vo.AgentStreamEventVO;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentSessionServiceImpl implements AgentSessionService {

    private final AgentOrchestrationService orchestrationService;
    private final Map<String, List<AgentMessageVO>> messages = new ConcurrentHashMap<>();

    public AgentSessionServiceImpl(AgentOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @Override
    public AgentSessionVO createSession(AuthenticatedPrincipal principal, AgentSessionCreateRequest request) {
        String id = "session-" + System.currentTimeMillis();
        messages.putIfAbsent(id, new ArrayList<>());
        return new AgentSessionVO(id, safe(request.agentKey(), "sage"), safe(request.title(), "Agent 会话"),
                safe(request.projectId(), "default"), safe(request.courseName(), ""), LocalDateTime.now(), true);
    }

    @Override
    public PageResponse<AgentMessageVO> listMessages(AuthenticatedPrincipal principal, String sessionId, long page, long pageSize) {
        List<AgentMessageVO> list = messages.getOrDefault(sessionId, List.of());
        return PageResponse.of(page, pageSize, list.size(), list);
    }

    @Override
    public AgentMessageSendVO sendMessage(AuthenticatedPrincipal principal, String sessionId, AgentMessageSendRequest request) {
        String messageId = "message-" + System.currentTimeMillis();
        String userId = principal == null || principal.id() == null ? "anonymous" : String.valueOf(principal.id());
        messages.computeIfAbsent(sessionId, key -> new ArrayList<>())
                .add(new AgentMessageVO(messageId, sessionId, "user", request.content(), LocalDateTime.now(), true));
        AgentRunResultVO result = orchestrationService.run(new AgentRunRequest(
                "request-" + messageId,
                userId,
                sessionId,
                messageId,
                request.content(),
                request.mode(),
                "default",
                "",
                "",
                Boolean.TRUE.equals(request.renderMermaid()),
                Map.of()
        ));
        String answerId = "assistant-" + messageId;
        messages.get(sessionId).add(new AgentMessageVO(answerId, sessionId, "assistant", result.answerText(), LocalDateTime.now(), true));
        return new AgentMessageSendVO(answerId, sessionId, result.answerText(), result.diagramScript(), result.diagramImagePath(), true);
    }

    @Override
    public List<AgentStreamEventVO> streamEvents(AuthenticatedPrincipal principal, String sessionId, String messageId) {
        return List.of(
                new AgentStreamEventVO("delta", messageId, "mock stream event from LangGraph4j skeleton", true),
                new AgentStreamEventVO("done", messageId, "", true)
        );
    }

    private String safe(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
