package com.hopeandsparks.explore.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.explore.dto.ExploreRequest;
import com.hopeandsparks.explore.dto.MindMapRequest;
import com.hopeandsparks.explore.service.ExploreService;
import com.hopeandsparks.explore.vo.ExploreVO;
import com.hopeandsparks.explore.vo.MindMapVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Nebula 探索入口，只负责探索流程和生成任务触发。
 */
@RestController
@RequestMapping("/api/v1/explore")
public class ExploreController {

    private final ExploreService exploreService;

    public ExploreController(ExploreService exploreService) {
        this.exploreService = exploreService;
    }

    @PostMapping
    public ApiResponse<ExploreVO> explore(
            Authentication authentication,
            @Valid @RequestBody ExploreRequest request
    ) {
        return ApiResponse.ok(exploreService.explore(principal(authentication), request));
    }

    @GetMapping("/{exploreId}")
    public ApiResponse<ExploreVO> detail(Authentication authentication, @PathVariable String exploreId) {
        return ApiResponse.ok(exploreService.detail(principal(authentication), exploreId));
    }

    @PostMapping("/{exploreId}/mindmap")
    public ApiResponse<MindMapVO> mindMap(
            Authentication authentication,
            @PathVariable String exploreId,
            @Valid @RequestBody(required = false) MindMapRequest request
    ) {
        return ApiResponse.ok(exploreService.createMindMap(principal(authentication), exploreId, request));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
