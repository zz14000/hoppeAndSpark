package com.hopeandsparks.resource.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.resource.service.ResourceService;
import com.hopeandsparks.resource.vo.ResourceDetailVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reading and code-case resource detail APIs.
 */
@RestController
public class ReadingController {

    private final ResourceService resourceService;

    public ReadingController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/api/v1/readings/{readingId}")
    public ApiResponse<ResourceDetailVO> reading(Authentication authentication, @PathVariable String readingId) {
        return ApiResponse.ok(resourceService.detail(principal(authentication), readingId));
    }

    @GetMapping("/api/v1/code-cases/{caseId}")
    public ApiResponse<ResourceDetailVO> codeCase(Authentication authentication, @PathVariable String caseId) {
        return ApiResponse.ok(resourceService.detail(principal(authentication), caseId));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
