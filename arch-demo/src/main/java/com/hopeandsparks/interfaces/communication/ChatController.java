package com.hopeandsparks.interfaces.communication;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接私信、群聊和好友申请接口；MVP 先保留路由，完整 WebSocket 消息系统后置实现。
 */
@RestController
@RequestMapping("/api/v1")
public class ChatController {

    @GetMapping("/chats")
    public ApiResponse<Map<String, Object>> chats(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "chat", "chats", Map.of(), query, null);
    }

    @GetMapping("/chats/{chatId}/messages")
    public ApiResponse<Map<String, Object>> messages(@PathVariable String chatId, @RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "chat", "messages", Map.of("chatId", chatId), query, null);
    }

    @PostMapping("/chats/{chatId}/messages")
    public ApiResponse<Map<String, Object>> sendMessage(@PathVariable String chatId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "chat", "sendMessage", Map.of("chatId", chatId), Map.of(), body);
    }

    @PostMapping("/chats/private")
    public ApiResponse<Map<String, Object>> createPrivateChat(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "chat", "createPrivateChat", Map.of(), Map.of(), body);
    }

    @PostMapping("/chats/groups")
    public ApiResponse<Map<String, Object>> createGroup(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "chat", "createGroup", Map.of(), Map.of(), body);
    }

    @PostMapping("/friend-requests")
    public ApiResponse<Map<String, Object>> friendRequest(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "chat", "friendRequest", Map.of(), Map.of(), body);
    }

    @PutMapping("/friend-requests/{requestId}")
    public ApiResponse<Map<String, Object>> handleFriendRequest(@PathVariable String requestId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "chat", "handleFriendRequest", Map.of("requestId", requestId), Map.of(), body);
    }
}
