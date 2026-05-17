package com.hopeandsparks.interfaces.agent;

import com.hopeandsparks.application.agent.AgentChatService;
import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.domain.agent.AgentMessageResult;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 文件职责：承接 Agent 智能体会话接口，包含会话创建、历史消息、普通消息发送和 SSE 流式响应入口。
 */
@RestController
@RequestMapping("/api/v1/agent-sessions")
public class AgentSessionController {

    private final AgentChatService agentChatService;

    public AgentSessionController(AgentChatService agentChatService) {
        this.agentChatService = agentChatService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createSession(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "agent", "createSession", Map.of(), Map.of(), body);
    }

    @GetMapping("/{sessionId}/messages")
    public ApiResponse<Map<String, Object>> messages(@PathVariable String sessionId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "agent", "messages", Map.of("sessionId", sessionId), Map.of(), null);
    }

    @PostMapping("/{sessionId}/messages")
    public ApiResponse<AgentMessageResult> sendMessage(
        @PathVariable String sessionId,
        @Valid @RequestBody SendAgentMessageRequest body,
        HttpServletRequest request
    ) {
        AgentMessageResult result = agentChatService.sendMessage(
            sessionId,
            body.agentCode(),
            body.userId(),
            body.content()
        );
        return ApiResponse.success(result, request.getAttribute("requestId").toString());
    }

    @GetMapping(value = "/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String sessionId) throws IOException {
        SseEmitter emitter = new SseEmitter(30_000L);
        emitter.send(SseEmitter.event()
            .name("message")
            .data(Map.of("sessionId", sessionId, "content", "mock stream event")));
        emitter.complete();
        return emitter;
    }
}
