package com.hopeandsparks.resource.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.resource.dto.ResourceExportRequest;
import com.hopeandsparks.resource.dto.ResourceFeedbackRequest;
import com.hopeandsparks.resource.dto.ResourceProgressUpdateRequest;
import com.hopeandsparks.resource.service.ResourceService;
import com.hopeandsparks.resource.vo.ResourceCardVO;
import com.hopeandsparks.resource.vo.ResourceDetailVO;
import com.hopeandsparks.resource.vo.ResourceExportVO;
import com.hopeandsparks.resource.vo.ResourceFeedbackVO;
import com.hopeandsparks.resource.vo.ResourceProgressVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Learning resource lifecycle APIs.
 */
@RestController
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/api/v1/resources")
    public ApiResponse<PageResponse<ResourceCardVO>> list(
            Authentication authentication,
            @RequestParam Map<String, String> query
    ) {
        return ApiResponse.ok(resourceService.listResources(principal(authentication), query));
    }

    @GetMapping("/api/v1/resources/{resourceId}")
    public ApiResponse<ResourceDetailVO> detail(Authentication authentication, @PathVariable String resourceId) {
        return ApiResponse.ok(resourceService.detail(principal(authentication), resourceId));
    }

    @PutMapping("/api/v1/resources/{resourceId}/progress")
    public ApiResponse<ResourceProgressVO> updateProgress(
            Authentication authentication,
            @PathVariable String resourceId,
            @Valid @RequestBody(required = false) ResourceProgressUpdateRequest request
    ) {
        return ApiResponse.ok(resourceService.updateProgress(principal(authentication), resourceId, request));
    }

    @PostMapping("/api/v1/resources/{resourceId}/export")
    public ApiResponse<ResourceExportVO> export(
            Authentication authentication,
            @PathVariable String resourceId,
            @RequestBody(required = false) ResourceExportRequest request
    ) {
        return ApiResponse.ok(resourceService.exportResource(principal(authentication), resourceId, request));
    }

    @PostMapping("/api/v1/resources/{resourceId}/feedback")
    public ApiResponse<ResourceFeedbackVO> feedback(
            Authentication authentication,
            @PathVariable String resourceId,
            @Valid @RequestBody ResourceFeedbackRequest request
    ) {
        return ApiResponse.ok(resourceService.feedback(principal(authentication), resourceId, request));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
