package com.hopeandsparks.interfaces.study;

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
 * 文件职责：承接学习计划、日历、拓扑、知识点资源网络和技能树接口。
 */
@RestController
@RequestMapping("/api/v1")
public class StudyController {

    @GetMapping("/learning-plans/current")
    public ApiResponse<Map<String, Object>> currentPlan(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "currentPlan");
    }

    @PostMapping("/learning-plans/generate")
    public ApiResponse<Map<String, Object>> generatePlan(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "generatePlan", Map.of(), Map.of(), body);
    }

    @PutMapping("/learning-plans/{planId}/adjust")
    public ApiResponse<Map<String, Object>> adjustPlan(@PathVariable String planId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "adjustPlan", Map.of("planId", planId), Map.of(), body);
    }

    @GetMapping("/learning-plans/{planId}/topology")
    public ApiResponse<Map<String, Object>> topology(@PathVariable String planId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "topology", Map.of("planId", planId), Map.of(), null);
    }

    @GetMapping("/learning-plans/{planId}/topology/nodes/{nodeId}/resource-network")
    public ApiResponse<Map<String, Object>> resourceNetwork(@PathVariable String planId, @PathVariable String nodeId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "resourceNetwork", Map.of("planId", planId, "nodeId", nodeId), Map.of(), null);
    }

    @GetMapping("/learning-plans/{planId}/topology/nodes/{nodeId}/resources")
    public ApiResponse<Map<String, Object>> nodeResources(@PathVariable String planId, @PathVariable String nodeId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "nodeResources", Map.of("planId", planId, "nodeId", nodeId), Map.of(), null);
    }

    @GetMapping("/calendar/events")
    public ApiResponse<Map<String, Object>> calendarEvents(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "calendarEvents", Map.of(), query, null);
    }

    @PostMapping("/calendar/events")
    public ApiResponse<Map<String, Object>> createEvent(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "createEvent", Map.of(), Map.of(), body);
    }

    @PutMapping("/calendar/events/{eventId}")
    public ApiResponse<Map<String, Object>> updateEvent(@PathVariable String eventId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "updateEvent", Map.of("eventId", eventId), Map.of(), body);
    }

    @DeleteMapping("/calendar/events/{eventId}")
    public ApiResponse<Map<String, Object>> deleteEvent(@PathVariable String eventId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "deleteEvent", Map.of("eventId", eventId), Map.of(), null);
    }

    @GetMapping("/skill-tree")
    public ApiResponse<Map<String, Object>> skillTree(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "skillTree");
    }

    @PostMapping("/skill-tree/nodes/{nodeId}/light-up")
    public ApiResponse<Map<String, Object>> lightUp(@PathVariable String nodeId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "study", "lightUp", Map.of("nodeId", nodeId), Map.of(), body);
    }
}
