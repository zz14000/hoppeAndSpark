package com.hopeandsparks.resource.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 学习资源接口，负责资源列表、详情、学习进度、导出和质量反馈。
 *
 * <p>生成类资源最终统一由 resource 模块落库，核心表包括 {@code learning_resource}、
 * {@code learning_resource_version}、{@code user_learning_record} 和收藏表。
 * 后续 Service 会处理资源状态、版本、质检结果和用户学习进度。</p>
 */
@RestController
public class ResourceController {

    @GetMapping("/api/v1/resources")
    public ApiResponse<Map<String, Object>> list(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("resource", "list", values("query", query)));
    }

    @GetMapping("/api/v1/resources/{resourceId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String resourceId) {
        return ApiResponse.ok(PlaceholderData.of("resource", "detail", values("resourceId", resourceId)));
    }

    @PutMapping("/api/v1/resources/{resourceId}/progress")
    public ApiResponse<Map<String, Object>> updateProgress(
            @PathVariable String resourceId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("resource", "updateProgress", values("resourceId", resourceId, "request", request)));
    }

    @PostMapping("/api/v1/resources/{resourceId}/export")
    public ApiResponse<Map<String, Object>> export(
            @PathVariable String resourceId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("resource", "export", values("resourceId", resourceId, "request", request)));
    }

    @PostMapping("/api/v1/resources/{resourceId}/feedback")
    public ApiResponse<Map<String, Object>> feedback(
            @PathVariable String resourceId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("resource", "feedback", values("resourceId", resourceId, "request", request)));
    }
}
