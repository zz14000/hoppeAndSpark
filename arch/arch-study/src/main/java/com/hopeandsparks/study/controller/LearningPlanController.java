package com.hopeandsparks.study.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 学习计划接口，负责当前计划、Strict 生成/调整计划、学习拓扑和知识点资源网络。
 *
 * <p>后续实现会读取用户画像、知识图谱、学习进度和资源数据，生成个性化 {@code study_plan}
 * 与 {@code study_task}。拓扑接口主要服务前端学习路径和知识网络页面。</p>
 */
@RestController
@RequestMapping("/api/v1/learning-plans")
public class LearningPlanController {

    @GetMapping("/current")
    public ApiResponse<Map<String, Object>> currentPlan() {
        return ApiResponse.ok(PlaceholderData.of("study", "currentPlan"));
    }

    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generatePlan(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("study", "generatePlan", values("request", request)));
    }

    @PutMapping("/{planId}/adjust")
    public ApiResponse<Map<String, Object>> adjustPlan(
            @PathVariable String planId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("study", "adjustPlan", values("planId", planId, "request", request)));
    }

    @GetMapping("/{planId}/topology")
    public ApiResponse<Map<String, Object>> topology(@PathVariable String planId) {
        return ApiResponse.ok(PlaceholderData.of("study", "topology", values("planId", planId)));
    }

    @GetMapping("/{planId}/topology/nodes/{nodeId}/resource-network")
    public ApiResponse<Map<String, Object>> resourceNetwork(@PathVariable String planId, @PathVariable String nodeId) {
        return ApiResponse.ok(PlaceholderData.of("study", "resourceNetwork", values("planId", planId, "nodeId", nodeId)));
    }

    @GetMapping("/{planId}/topology/nodes/{nodeId}/resources")
    public ApiResponse<Map<String, Object>> nodeResources(@PathVariable String planId, @PathVariable String nodeId) {
        return ApiResponse.ok(PlaceholderData.of("study", "nodeResources", values("planId", planId, "nodeId", nodeId)));
    }
}
