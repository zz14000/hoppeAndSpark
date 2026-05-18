package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 后台审核与风控接口，负责内容审核记录和用户行为预警处理。
 *
 * <p>MVP 先覆盖社区文章/评论的审核结果处理；后续可以扩展行为风控、AI 审核记录、
 * 人工复核和审计报表。这里仍然只做后台入口，具体状态更新调用对应业务模块。</p>
 */
@RestController
@RequestMapping("/api/v1/manage/moderation")
public class ManageModerationController {

    @GetMapping("/content")
    public ApiResponse<Map<String, Object>> content(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "moderationContent", values("query", query)));
    }

    @PutMapping("/content/{recordId}")
    public ApiResponse<Map<String, Object>> handleContent(
            @PathVariable String recordId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "handleModerationContent", values("recordId", recordId, "request", request)));
    }

    @GetMapping("/behavior-alerts")
    public ApiResponse<Map<String, Object>> behaviorAlerts(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "behaviorAlerts", values("query", query)));
    }

    @PutMapping("/behavior-alerts/{alertId}")
    public ApiResponse<Map<String, Object>> handleBehaviorAlert(
            @PathVariable String alertId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "handleBehaviorAlert", values("alertId", alertId, "request", request)));
    }
}
