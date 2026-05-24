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
 * Manage behavior alert entry.
 */
@RestController
@RequestMapping("/api/v1/manage/moderation/behavior-alerts")
public class ManageBehaviorAlertController {

    @GetMapping
    public ApiResponse<Map<String, Object>> behaviorAlerts(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "behaviorAlerts", values("query", query)));
    }

    @PutMapping("/{alertId}")
    public ApiResponse<Map<String, Object>> handleBehaviorAlert(
            @PathVariable String alertId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "handleBehaviorAlert", values("alertId", alertId, "request", request)));
    }
}
