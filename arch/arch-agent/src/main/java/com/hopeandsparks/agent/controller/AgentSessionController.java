package com.hopeandsparks.agent.controller;

import com.hopeandsparks.agent.dto.AgentMessageSendRequest;
import com.hopeandsparks.agent.dto.AgentSessionCreateRequest;
import com.hopeandsparks.agent.service.AgentSessionService;
import com.hopeandsparks.agent.vo.AgentMessageSendVO;
import com.hopeandsparks.agent.vo.AgentMessageVO;
import com.hopeandsparks.agent.vo.AgentSessionVO;
import com.hopeandsparks.agent.vo.AgentStreamEventVO;
import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * 智能体会话接口，负责本地会话、消息历史、发送消息和 SSE 输出。
 */
@RestController
@RequestMapping("/api/v1/agent-sessions")
public class AgentSessionController {

    private final AgentSessionService agentSessionService;

    public AgentSessionController(AgentSessionService agentSessionService) {
        this.agentSessionService = agentSessionService;
    }

    @PostMapping
    public ApiResponse<AgentSessionVO> createSession(
            Authentication authentication,
            @Valid @RequestBody AgentSessionCreateRequest request
    ) {
        return ApiResponse.ok(agentSessionService.createSession(principal(authentication), request));
    }

    @GetMapping("/{sessionId}/messages")
    public ApiResponse<PageResponse<AgentMessageVO>> messages(
            Authentication authentication,
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "30") long pageSize
    ) {
        return ApiResponse.ok(agentSessionService.listMessages(principal(authentication), sessionId, page, pageSize));
    }

    @PostMapping("/{sessionId}/messages")
    public ApiResponse<AgentMessageSendVO> sendMessage(
            Authentication authentication,
            @PathVariable String sessionId,
            @Valid @RequestBody AgentMessageSendRequest request
    ) {
        return ApiResponse.ok(agentSessionService.sendMessage(principal(authentication), sessionId, request));
    }

    @GetMapping(value = "/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            Authentication authentication,
            @PathVariable String sessionId,
            @RequestParam String messageId
    ) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            for (AgentStreamEventVO event : agentSessionService.streamEvents(principal(authentication), sessionId, messageId)) {
                emitter.send(SseEmitter.event().name(event.type()).data(eventData(event)));
            }
            emitter.complete();
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    private Map<String, Object> eventData(AgentStreamEventVO event) {
        if ("done".equals(event.type())) {
            return Map.of("messageId", event.messageId(), "payload", event.payload(), "mock", event.mock());
        }
        return Map.of(
                "content", event.content(),
                "messageId", event.messageId(),
                "payload", event.payload(),
                "stage", event.type(),
                "mock", event.mock()
        );
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
