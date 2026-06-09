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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Document resource APIs.
 */
@RestController
public class DocumentController {

    private final ResourceService resourceService;

    public DocumentController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/api/v1/documents/{documentId}")
    public ApiResponse<ResourceDetailVO> document(Authentication authentication, @PathVariable String documentId) {
        return ApiResponse.ok(resourceService.detail(principal(authentication), documentId));
    }

    @GetMapping("/api/v1/documents/{documentId}/outline")
    public ApiResponse<Map<String, Object>> outline(Authentication authentication, @PathVariable String documentId) {
        ResourceDetailVO detail = resourceService.detail(principal(authentication), documentId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("resourceId", detail.id());
        data.put("title", detail.title());
        data.put("sections", List.of());
        return ApiResponse.ok(data);
    }

    @PutMapping("/api/v1/documents/{documentId}/reading-progress")
    public ApiResponse<ResourceProgressVO> readingProgress(
            Authentication authentication,
            @PathVariable String documentId,
            @Valid @RequestBody(required = false) ResourceProgressUpdateRequest request
    ) {
        return ApiResponse.ok(resourceService.updateProgress(principal(authentication), documentId, request));
    }

    @PostMapping("/api/v1/documents/{documentId}/ask")
    public ApiResponse<Map<String, Object>> ask(
            Authentication authentication,
            @PathVariable String documentId,
            @RequestBody(required = false) Map<String, Object> request
    ) {
        ResourceDetailVO detail = resourceService.detail(principal(authentication), documentId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("resourceId", detail.id());
        data.put("answer", "Sage 伴读接入先空着：已收到问题，后续会结合文档切片和知识库生成回答。");
        data.put("citations", List.of());
        data.put("request", request);
        return ApiResponse.ok(data);
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
