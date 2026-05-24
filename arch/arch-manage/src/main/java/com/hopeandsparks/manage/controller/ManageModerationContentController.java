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
 * Manage content moderation entry.
 */
@RestController
@RequestMapping("/api/v1/manage/moderation/content")
public class ManageModerationContentController {

    @GetMapping
    public ApiResponse<Map<String, Object>> content(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "moderationContent", values("query", query)));
    }

    @PutMapping("/{recordId}")
    public ApiResponse<Map<String, Object>> handleContent(
            @PathVariable String recordId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "handleModerationContent", values("recordId", recordId, "request", request)));
    }
}
