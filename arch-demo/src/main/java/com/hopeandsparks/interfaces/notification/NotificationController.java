package com.hopeandsparks.interfaces.notification;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接通知列表、已读、删除和通知动作回调接口。
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @GetMapping
    public ApiResponse<Map<String, Object>> notifications(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "notification", "list", Map.of(), query, null);
    }

    @PutMapping("/{notificationId}/read")
    public ApiResponse<Map<String, Object>> read(@PathVariable String notificationId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "notification", "read", Map.of("notificationId", notificationId), Map.of(), null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Map<String, Object>> readAll(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "notification", "readAll");
    }

    @DeleteMapping("/{notificationId}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable String notificationId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "notification", "delete", Map.of("notificationId", notificationId), Map.of(), null);
    }

    @PostMapping("/{notificationId}/actions")
    public ApiResponse<Map<String, Object>> action(@PathVariable String notificationId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "notification", "action", Map.of("notificationId", notificationId), Map.of(), body);
    }
}
