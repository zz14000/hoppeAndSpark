package com.hopeandsparks.knowledge.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.knowledge.service.KnowledgeGraphService;
import com.hopeandsparks.knowledge.vo.KnowledgeGraphVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Knowledge graph API for course nodes and node relations.
 */
@RestController
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    public KnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
        this.knowledgeGraphService = knowledgeGraphService;
    }

    @GetMapping("/api/v1/knowledge-graph")
    public ApiResponse<KnowledgeGraphVO> graph(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int depth
    ) {
        return ApiResponse.ok(knowledgeGraphService.graph(keyword, depth));
    }
}
