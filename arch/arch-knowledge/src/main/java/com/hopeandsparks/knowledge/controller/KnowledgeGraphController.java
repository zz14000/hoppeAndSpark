package com.hopeandsparks.knowledge.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 知识图谱接口，用于查询课程、知识点和知识点关系组成的图结构。
 *
 * <p>后续会读取 {@code course}、{@code knowledge_node}、{@code knowledge_node_relation}
 * 等表，为探索、学习拓扑和资源推荐提供基础结构数据。</p>
 */
@RestController
public class KnowledgeGraphController {

    @GetMapping("/api/v1/knowledge-graph")
    public ApiResponse<Map<String, Object>> graph(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("knowledge", "graph", values("query", query)));
    }
}
