package com.hopeandsparks.community.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.community.service.FollowService;
import com.hopeandsparks.community.vo.FollowResultVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Community follow APIs.
 */
@RestController
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/api/v1/users/{userId}/follow")
    public ApiResponse<FollowResultVO> follow(Authentication authentication, @PathVariable String userId) {
        return ApiResponse.ok(followService.follow(principal(authentication), userId));
    }

    @DeleteMapping("/api/v1/users/{userId}/follow")
    public ApiResponse<FollowResultVO> unfollow(Authentication authentication, @PathVariable String userId) {
        return ApiResponse.ok(followService.unfollow(principal(authentication), userId));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
