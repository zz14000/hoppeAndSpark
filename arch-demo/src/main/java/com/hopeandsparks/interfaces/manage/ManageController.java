package com.hopeandsparks.interfaces.manage;

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
 * 文件职责：承接 Manage 管理端接口，包含管理员登录、数据看板、用户管理、知识库、工单、资源、Prompt 和审核风控。
 */
@RestController
@RequestMapping("/api/v1/manage")
public class ManageController {

    @PostMapping("/auth/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "login", Map.of(), Map.of(), body);
    }

    @GetMapping("/dashboard/overview")
    public ApiResponse<Map<String, Object>> overview(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "overview");
    }

    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> users(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "users", Map.of(), query, null);
    }

    @PostMapping("/users/{userId}/actions")
    public ApiResponse<Map<String, Object>> userAction(@PathVariable String userId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "userAction", Map.of("userId", userId), Map.of(), body);
    }

    @GetMapping("/users/{userId}/learning-trace")
    public ApiResponse<Map<String, Object>> learningTrace(@PathVariable String userId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "learningTrace", Map.of("userId", userId), Map.of(), null);
    }

    @GetMapping("/knowledge-base/documents")
    public ApiResponse<Map<String, Object>> kbDocuments(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "kbDocuments", Map.of(), query, null);
    }

    @PostMapping("/knowledge-base/documents")
    public ApiResponse<Map<String, Object>> uploadKbDocument(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "uploadKbDocument", Map.of(), Map.of(), body);
    }

    @PutMapping("/knowledge-base/documents/{documentId}")
    public ApiResponse<Map<String, Object>> updateKbDocument(@PathVariable String documentId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "updateKbDocument", Map.of("documentId", documentId), Map.of(), body);
    }

    @DeleteMapping("/knowledge-base/documents/{documentId}")
    public ApiResponse<Map<String, Object>> deleteKbDocument(@PathVariable String documentId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "deleteKbDocument", Map.of("documentId", documentId), Map.of(), null);
    }

    @GetMapping("/knowledge-base/documents/{documentId}/parse-status")
    public ApiResponse<Map<String, Object>> parseStatus(@PathVariable String documentId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "parseStatus", Map.of("documentId", documentId), Map.of(), null);
    }

    @GetMapping("/ai-disputes")
    public ApiResponse<Map<String, Object>> disputes(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "disputes", Map.of(), query, null);
    }

    @PutMapping("/ai-disputes/{disputeId}")
    public ApiResponse<Map<String, Object>> handleDispute(@PathVariable String disputeId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "handleDispute", Map.of("disputeId", disputeId), Map.of(), body);
    }

    @GetMapping("/storage/overview")
    public ApiResponse<Map<String, Object>> storage(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "storage");
    }

    @GetMapping("/resources")
    public ApiResponse<Map<String, Object>> resources(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "resources", Map.of(), query, null);
    }

    @DeleteMapping("/resources/{resourceId}")
    public ApiResponse<Map<String, Object>> deleteResource(@PathVariable String resourceId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "deleteResource", Map.of("resourceId", resourceId), Map.of(), null);
    }

    @GetMapping("/agent-prompts")
    public ApiResponse<Map<String, Object>> prompts(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "prompts");
    }

    @PutMapping("/agent-prompts/{promptId}")
    public ApiResponse<Map<String, Object>> updatePrompt(@PathVariable String promptId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "updatePrompt", Map.of("promptId", promptId), Map.of(), body);
    }

    @GetMapping("/moderation/content")
    public ApiResponse<Map<String, Object>> moderationContent(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "moderationContent", Map.of(), query, null);
    }

    @PutMapping("/moderation/content/{recordId}")
    public ApiResponse<Map<String, Object>> handleModerationContent(@PathVariable String recordId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "handleModerationContent", Map.of("recordId", recordId), Map.of(), body);
    }

    @GetMapping("/moderation/behavior-alerts")
    public ApiResponse<Map<String, Object>> behaviorAlerts(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "behaviorAlerts", Map.of(), query, null);
    }

    @PutMapping("/moderation/behavior-alerts/{alertId}")
    public ApiResponse<Map<String, Object>> handleBehaviorAlert(@PathVariable String alertId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "manage", "handleBehaviorAlert", Map.of("alertId", alertId), Map.of(), body);
    }
}
