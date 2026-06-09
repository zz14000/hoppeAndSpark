package com.hopeandsparks.resource.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.resource.dto.ResourceProgressUpdateRequest;
import com.hopeandsparks.resource.service.ResourceService;
import com.hopeandsparks.resource.vo.ResourceDetailVO;
import com.hopeandsparks.resource.vo.ResourceProgressVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Video resource APIs.
 */
@RestController
public class VideoController {

    private final ResourceService resourceService;

    public VideoController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/api/v1/videos/{videoId}")
    public ApiResponse<ResourceDetailVO> video(Authentication authentication, @PathVariable String videoId) {
        return ApiResponse.ok(resourceService.detail(principal(authentication), videoId));
    }

    @GetMapping("/api/v1/videos/{videoId}/episodes")
    public ApiResponse<Map<String, Object>> episodes(Authentication authentication, @PathVariable String videoId) {
        ResourceDetailVO detail = resourceService.detail(principal(authentication), videoId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("resourceId", detail.id());
        data.put("episodes", List.of());
        return ApiResponse.ok(data);
    }

    @PutMapping("/api/v1/videos/{videoId}/watch-progress")
    public ApiResponse<ResourceProgressVO> watchProgress(
            Authentication authentication,
            @PathVariable String videoId,
            @Valid @RequestBody(required = false) ResourceProgressUpdateRequest request
    ) {
        return ApiResponse.ok(resourceService.updateProgress(principal(authentication), videoId, request));
    }

    @GetMapping("/api/v1/videos/{videoId}/transcripts")
    public ApiResponse<Map<String, Object>> transcripts(Authentication authentication, @PathVariable String videoId) {
        ResourceDetailVO detail = resourceService.detail(principal(authentication), videoId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("resourceId", detail.id());
        data.put("transcripts", List.of());
        return ApiResponse.ok(data);
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
