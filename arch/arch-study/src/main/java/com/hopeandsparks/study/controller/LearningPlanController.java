package com.hopeandsparks.study.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.resource.vo.ResourceCardVO;
import com.hopeandsparks.study.dto.PlanAdjustRequest;
import com.hopeandsparks.study.dto.PlanGenerateRequest;
import com.hopeandsparks.study.service.LearningPlanService;
import com.hopeandsparks.study.vo.LearningPlanVO;
import com.hopeandsparks.study.vo.ResourceNetworkVO;
import com.hopeandsparks.study.vo.TopologyVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Learning plan APIs for current plan, generation, adjustment and topology.
 */
@RestController
@RequestMapping("/api/v1/learning-plans")
public class LearningPlanController {

    private final LearningPlanService learningPlanService;

    public LearningPlanController(LearningPlanService learningPlanService) {
        this.learningPlanService = learningPlanService;
    }

    @GetMapping("/current")
    public ApiResponse<LearningPlanVO> currentPlan(Authentication authentication) {
        return ApiResponse.ok(learningPlanService.currentPlan(principal(authentication)));
    }

    @PostMapping("/generate")
    public ApiResponse<LearningPlanVO> generatePlan(
            Authentication authentication,
            @Valid @RequestBody(required = false) PlanGenerateRequest request
    ) {
        return ApiResponse.ok(learningPlanService.generatePlan(principal(authentication), request));
    }

    @PutMapping("/{planId}/adjust")
    public ApiResponse<LearningPlanVO> adjustPlan(
            Authentication authentication,
            @PathVariable String planId,
            @RequestBody(required = false) PlanAdjustRequest request
    ) {
        return ApiResponse.ok(learningPlanService.adjustPlan(principal(authentication), planId, request));
    }

    @GetMapping("/{planId}/topology")
    public ApiResponse<TopologyVO> topology(Authentication authentication, @PathVariable String planId) {
        return ApiResponse.ok(learningPlanService.topology(principal(authentication), planId));
    }

    @GetMapping("/{planId}/topology/nodes/{nodeId}/resource-network")
    public ApiResponse<ResourceNetworkVO> resourceNetwork(
            Authentication authentication,
            @PathVariable String planId,
            @PathVariable String nodeId
    ) {
        return ApiResponse.ok(learningPlanService.resourceNetwork(principal(authentication), planId, nodeId));
    }

    @GetMapping("/{planId}/topology/nodes/{nodeId}/resources")
    public ApiResponse<PageResponse<ResourceCardVO>> nodeResources(
            Authentication authentication,
            @PathVariable String planId,
            @PathVariable String nodeId,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize
    ) {
        return ApiResponse.ok(learningPlanService.nodeResources(
                principal(authentication),
                planId,
                nodeId,
                type,
                page,
                pageSize
        ));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
