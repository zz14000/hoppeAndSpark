package com.hopeandsparks.im.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 聊天接口，负责会话列表、消息列表、发送消息、创建私聊和创建群聊。
 *
 * <p>IM 属于后置模块，当前先根据 API 文档保留接口契约。后续实现时会读写
 * {@code im_conversation}、{@code im_message}、群组和已读表，并可以再接 WebSocket。</p>
 */
@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    @GetMapping
    public ApiResponse<Map<String, Object>> chats(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("im", "chats", values("query", query)));
    }

    @GetMapping("/{chatId}/messages")
    public ApiResponse<Map<String, Object>> messages(@PathVariable String chatId, @RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("im", "messages", values("chatId", chatId, "query", query)));
    }

    @PostMapping("/{chatId}/messages")
    public ApiResponse<Map<String, Object>> sendMessage(
            @PathVariable String chatId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("im", "sendMessage", values("chatId", chatId, "request", request)));
    }

    @PostMapping("/private")
    public ApiResponse<Map<String, Object>> createPrivateChat(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("im", "createPrivateChat", values("request", request)));
    }

    @PostMapping("/groups")
    public ApiResponse<Map<String, Object>> createGroup(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("im", "createGroup", values("request", request)));
    }
}
