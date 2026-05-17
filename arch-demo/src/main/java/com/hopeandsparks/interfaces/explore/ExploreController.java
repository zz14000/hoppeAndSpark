package com.hopeandsparks.interfaces.explore;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接 Nebula 探索、探索详情、思维导图和知识关联图谱接口。
 */
@RestController
@RequestMapping("/api/v1")
public class ExploreController {

    @PostMapping("/explore")
    public ApiResponse<Map<String, Object>> explore(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "explore", "create", Map.of(), Map.of(), body);
    }

    @GetMapping("/explore/{exploreId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String exploreId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "explore", "detail", Map.of("exploreId", exploreId), Map.of(), null);
    }

    @PostMapping("/explore/{exploreId}/mindmap")
    public ApiResponse<Map<String, Object>> mindmap(@PathVariable String exploreId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "explore", "mindmap", Map.of("exploreId", exploreId), Map.of(), body);
    }

    @GetMapping("/knowledge-graph")
    public ApiResponse<Map<String, Object>> knowledgeGraph(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "explore", "knowledgeGraph");
    }
}
