package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.AgentMessageSendRequest;
import com.hopeandsparks.agent.dto.AgentSessionCreateRequest;
import com.hopeandsparks.agent.entity.AgentChatMessage;
import com.hopeandsparks.agent.entity.AgentChatSession;
import com.hopeandsparks.agent.service.AgentCatalogService;
import com.hopeandsparks.agent.service.AgentSessionService;
import com.hopeandsparks.agent.vo.AgentInfoVO;
import com.hopeandsparks.agent.vo.AgentMessageSendVO;
import com.hopeandsparks.agent.vo.AgentMessageVO;
import com.hopeandsparks.agent.vo.AgentSessionVO;
import com.hopeandsparks.agent.vo.AgentStreamEventVO;
import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.coze.CozeAgentClient;
import com.hopeandsparks.infra.coze.CozeAgentRequest;
import com.hopeandsparks.infra.coze.CozeAgentResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent 会话 W3 内存实现。先跑通会话、消息、SSE 和 mock Coze 网关边界。
 */
@Service
public class InMemoryAgentSessionService implements AgentSessionService {

    private final AgentCatalogService agentCatalogService;
    private final CozeAgentClient cozeAgentClient;
    private final Map<String, AgentChatSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, List<AgentChatMessage>> messagesBySession = new ConcurrentHashMap<>();
    private final Map<String, AgentChatMessage> messagesById = new ConcurrentHashMap<>();
    private final AtomicLong sessionSequence = new AtomicLong(1001);
    private final AtomicLong messageSequence = new AtomicLong(1001);

    public InMemoryAgentSessionService(AgentCatalogService agentCatalogService, CozeAgentClient cozeAgentClient) {
        this.agentCatalogService = agentCatalogService;
        this.cozeAgentClient = cozeAgentClient;
    }

    @Override
    public AgentSessionVO createSession(AuthenticatedPrincipal principal, AgentSessionCreateRequest request) {
        Long userId = requireUserId(principal);
        if (request == null) {
            throw new BusinessException(400, "请求体不能为空");
        }
        AgentInfoVO agent = agentCatalogService.getAgent(request.agentId());
        LocalDateTime now = LocalDateTime.now();

        AgentChatSession session = new AgentChatSession();
        session.setSessionId(nextSessionId());
        session.setUserId(userId);
        session.setAgentId(agent.id());
        session.setTitle(firstText(request.title(), agent.name() + " 对话"));
        session.setSource(firstText(request.source(), "agent_chat"));
        session.setContext(request.context());
        session.setMetadata(request.metadata());
        session.setContextNodeId(firstText(request.contextNodeId(), stringFromMap(request.context(), "nodeId")));
        session.setContextResourceId(firstText(request.contextResourceId(), stringFromMap(request.context(), "resourceId")));
        session.setStatus("active");
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        sessions.put(session.getSessionId(), session);
        messagesBySession.put(session.getSessionId(), new ArrayList<>());
        return toSessionVO(session);
    }

    @Override
    public PageResponse<AgentMessageVO> listMessages(AuthenticatedPrincipal principal, String sessionId, long page, long pageSize) {
        AgentChatSession session = requireSession(principal, sessionId);
        List<AgentMessageVO> all = messagesBySession.getOrDefault(session.getSessionId(), List.of()).stream()
                .map(this::toMessageVO)
                .toList();
        int fromIndex = (int) Math.min((Math.max(page, 1) - 1) * Math.max(pageSize, 1), all.size());
        int toIndex = (int) Math.min(fromIndex + Math.max(pageSize, 1), all.size());
        return PageResponse.of(page, pageSize, all.size(), all.subList(fromIndex, toIndex));
    }

    @Override
    public synchronized AgentMessageSendVO sendMessage(AuthenticatedPrincipal principal, String sessionId, AgentMessageSendRequest request) {
        AgentChatSession session = requireSession(principal, sessionId);
        if (request == null || isBlank(request.content())) {
            throw new BusinessException(400, "消息内容不能为空");
        }

        AgentChatMessage userMessage = buildUserMessage(session, request);
        addMessage(userMessage);

        CozeAgentResponse response = callMockGateway(session, userMessage);
        session.setExternalConversationId(firstText(session.getExternalConversationId(), response.externalConversationId()));
        session.setUpdatedAt(LocalDateTime.now());

        AgentChatMessage reply = buildAssistantMessage(session, userMessage, response);
        addMessage(reply);
        return new AgentMessageSendVO(userMessage.getMessageId(), toMessageVO(reply));
    }

    @Override
    public List<AgentStreamEventVO> streamEvents(AuthenticatedPrincipal principal, String sessionId, String messageId) {
        requireSession(principal, sessionId);
        if (isBlank(messageId)) {
            throw new BusinessException(400, "messageId 不能为空");
        }
        AgentChatMessage target = messagesById.get(messageId);
        if (target == null || !sessionId.equals(target.getSessionId())) {
            throw new BusinessException(404, "消息不存在");
        }
        AgentChatMessage reply = "assistant".equals(target.getRole()) ? target : findReply(sessionId, messageId);
        if (reply == null) {
            throw new BusinessException(404, "该消息还没有可流式读取的回复");
        }

        List<AgentStreamEventVO> events = new ArrayList<>();
        for (String chunk : splitContent(reply.getContent())) {
            events.add(new AgentStreamEventVO("chunk", chunk, reply.getMessageId(), reply.isMock()));
        }
        events.add(new AgentStreamEventVO("done", null, reply.getMessageId(), reply.isMock()));
        return events;
    }

    private AgentChatMessage buildUserMessage(AgentChatSession session, AgentMessageSendRequest request) {
        AgentChatMessage message = new AgentChatMessage();
        message.setMessageId(nextMessageId());
        message.setSessionId(session.getSessionId());
        message.setRole("user");
        message.setContentType(firstText(request.contentType(), "text"));
        message.setContent(request.content());
        message.setAgentId(session.getAgentId());
        message.setAttachments(request.attachments() == null ? List.of() : request.attachments());
        message.setMock(false);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    private AgentChatMessage buildAssistantMessage(AgentChatSession session, AgentChatMessage userMessage, CozeAgentResponse response) {
        AgentChatMessage message = new AgentChatMessage();
        message.setMessageId(nextMessageId());
        message.setSessionId(session.getSessionId());
        message.setRole("assistant");
        message.setContentType("text");
        message.setContent(mockReply(session.getAgentId(), userMessage.getContent()));
        message.setAgentId(session.getAgentId());
        message.setParentMessageId(userMessage.getMessageId());
        message.setAttachments(List.of());
        message.setExternalMessageId(response.externalMessageId());
        message.setRawResponse(Map.of(
                "agentCode", response.agentCode(),
                "output", response.output(),
                "externalConversationId", response.externalConversationId(),
                "mock", response.mock()
        ));
        message.setMock(response.mock());
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    private CozeAgentResponse callMockGateway(AgentChatSession session, AgentChatMessage userMessage) {
        Map<String, String> context = new LinkedHashMap<>();
        context.put("sessionId", session.getSessionId());
        context.put("source", session.getSource());
        context.put("task_id", "chat_" + userMessage.getMessageId());
        context.put("task_type", "agent_chat");
        context.put("target_agent", session.getAgentId());
        putIfPresent(context, "contextNodeId", session.getContextNodeId());
        putIfPresent(context, "contextResourceId", session.getContextResourceId());

        CozeAgentRequest request = new CozeAgentRequest(
                session.getAgentId(),
                userMessage.getContent(),
                session.getExternalConversationId(),
                context
        );
        if (agentCatalogService.isWorkflowAgent(session.getAgentId())) {
            return cozeAgentClient.runWorkflow(request);
        }
        return cozeAgentClient.chat(request);
    }

    private void addMessage(AgentChatMessage message) {
        messagesBySession.computeIfAbsent(message.getSessionId(), id -> new ArrayList<>()).add(message);
        messagesById.put(message.getMessageId(), message);
    }

    private AgentChatMessage findReply(String sessionId, String userMessageId) {
        return messagesBySession.getOrDefault(sessionId, List.of()).stream()
                .filter(message -> "assistant".equals(message.getRole()))
                .filter(message -> userMessageId.equals(message.getParentMessageId()))
                .findFirst()
                .orElse(null);
    }

    private AgentChatSession requireSession(AuthenticatedPrincipal principal, String sessionId) {
        Long userId = requireUserId(principal);
        AgentChatSession session = sessions.get(sessionId);
        if (session == null) {
            throw new BusinessException(404, "智能体会话不存在");
        }
        if (!userId.equals(session.getUserId())) {
            throw new BusinessException(403, "不能访问其他用户的智能体会话");
        }
        return session;
    }

    private AgentSessionVO toSessionVO(AgentChatSession session) {
        return new AgentSessionVO(
                session.getSessionId(),
                session.getAgentId(),
                session.getTitle(),
                session.getSource(),
                session.getContextNodeId(),
                session.getContextResourceId(),
                session.getStatus(),
                session.getExternalConversationId(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private AgentMessageVO toMessageVO(AgentChatMessage message) {
        return new AgentMessageVO(
                message.getMessageId(),
                message.getSessionId(),
                message.getRole(),
                message.getContentType(),
                message.getContent(),
                message.getAgentId(),
                message.getAttachments() == null ? List.of() : message.getAttachments(),
                message.getExternalMessageId(),
                message.isMock(),
                message.getCreatedAt()
        );
    }

    private List<String> splitContent(String content) {
        if (content == null || content.isBlank()) {
            return List.of("");
        }
        int size = Math.max(12, Math.min(32, content.length() / 3 + 1));
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < content.length(); start += size) {
            chunks.add(content.substring(start, Math.min(start + size, content.length())));
        }
        return chunks;
    }

    private String mockReply(String agentId, String content) {
        String safeContent = content == null ? "" : content.trim();
        return switch (agentId.toLowerCase(Locale.ROOT)) {
            case "sage" -> "Sage mock：我们先把问题拆小。你问的是「" + safeContent + "」。先确认定义、边界条件，再看一个反例会更稳。";
            case "coach" -> "Coach mock：我会先判断错因，再给提示。当前建议：先写出你的中间步骤，我再帮你定位卡点。";
            case "ava" -> "Ava mock：先别急着一次做完。我们只完成一个小目标：把这个问题最核心的一句话写清楚。";
            case "strict" -> "Strict mock：我会把目标拆成可执行任务。当前先生成计划草案，真实 Strict 工作流后续再接入。";
            case "nebula" -> "Nebula mock：已收到探索主题「" + safeContent + "」，真实资源生成先通过 arch-explore 入口触发。";
            case "horizon" -> "Horizon mock：我会检查事实、结构和风险点。真实质检工作流当前暂不接入。";
            default -> "Agent mock：已收到你的消息「" + safeContent + "」。真实 Coze 接入先保持为空。";
        };
    }

    private String nextSessionId() {
        return "ags_" + sessionSequence.getAndIncrement();
    }

    private String nextMessageId() {
        return "msg_" + messageSequence.getAndIncrement();
    }

    private Long requireUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            throw new BusinessException(401, "请先登录前台账号");
        }
        return principal.id();
    }

    private String stringFromMap(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return null;
        }
        return String.valueOf(map.get(key));
    }

    private void putIfPresent(Map<String, String> map, String key, String value) {
        if (!isBlank(value)) {
            map.put(key, value);
        }
    }

    private String firstText(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
