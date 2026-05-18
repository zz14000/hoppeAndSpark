package com.hopeandsparks.explore.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * Nebula 探索接口，负责全局探索、探索详情和思维导图生成触发。
 *
 * <p>按照架构边界，explore 只负责探索入口和生成任务触发；异步任务状态交给
 * {@code arch-task}，生成出来的学习资源由 {@code arch-resource} 落库。</p>
 */
@RestController
@RequestMapping("/api/v1/explore")
public class ExploreController {

    @PostMapping
    public ApiResponse<Map<String, Object>> explore(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("explore", "explore", values("request", request)));
    }

    @GetMapping("/{exploreId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String exploreId) {
        return ApiResponse.ok(PlaceholderData.of("explore", "detail", values("exploreId", exploreId)));
    }

    @PostMapping("/{exploreId}/mindmap")
    public ApiResponse<Map<String, Object>> mindMap(
            @PathVariable String exploreId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("explore", "mindMap", values("exploreId", exploreId, "request", request)));
    }
}
