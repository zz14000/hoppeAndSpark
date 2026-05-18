package com.hopeandsparks.notification.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 通知接口，负责通知列表、已读、全部已读、删除和通知动作回调。
 *
 * <p>通知来源可能是学习提醒、社区互动、好友申请、后台审核结果等。当前先保留 REST 契约，
 * 后续可扩展站内信、WebSocket 推送或移动端推送。</p>
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("notification", "list", values("query", query)));
    }

    @PutMapping("/{notificationId}/read")
    public ApiResponse<Map<String, Object>> read(@PathVariable String notificationId) {
        return ApiResponse.ok(PlaceholderData.of("notification", "read", values("notificationId", notificationId)));
    }

    @PutMapping("/read-all")
    public ApiResponse<Map<String, Object>> readAll() {
        return ApiResponse.ok(PlaceholderData.of("notification", "readAll"));
    }

    @DeleteMapping("/{notificationId}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable String notificationId) {
        return ApiResponse.ok(PlaceholderData.of("notification", "delete", values("notificationId", notificationId)));
    }

    @PostMapping("/{notificationId}/actions")
    public ApiResponse<Map<String, Object>> action(
            @PathVariable String notificationId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("notification", "action", values("notificationId", notificationId, "request", request)));
    }
}
