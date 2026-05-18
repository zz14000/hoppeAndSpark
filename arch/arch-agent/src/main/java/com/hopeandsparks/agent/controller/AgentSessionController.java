package com.hopeandsparks.agent.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.http.MediaType;
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

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 智能体会话接口，负责创建本地会话、查询历史消息、发送消息和 SSE 流式响应。
 *
 * <p>后续实现时，这里会调用 Agent Service 写入 {@code agent_chat_session} 和
 * {@code agent_chat_message}，再通过 infra 的 Coze 客户端完成实际 Bot / Workflow 调用。
 * SSE 接口用于对话型 Agent 的实时输出。</p>
 */
@RestController
@RequestMapping("/api/v1/agent-sessions")
public class AgentSessionController {

    @PostMapping
    public ApiResponse<Map<String, Object>> createSession(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("agent", "createSession", values("request", request)));
    }

    @GetMapping("/{sessionId}/messages")
    public ApiResponse<Map<String, Object>> messages(@PathVariable String sessionId, @RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("agent", "messages", values("sessionId", sessionId, "query", query)));
    }

    @PostMapping("/{sessionId}/messages")
    public ApiResponse<Map<String, Object>> sendMessage(
            @PathVariable String sessionId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("agent", "sendMessage", values("sessionId", sessionId, "request", request)));
    }

    @GetMapping(value = "/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String sessionId, @RequestParam Map<String, String> query) throws IOException {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.send(SseEmitter.event()
                .name("message")
                .data(PlaceholderData.of("agent", "stream", values("sessionId", sessionId, "query", query))));
        emitter.complete();
        return emitter;
    }
}
