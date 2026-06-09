package com.hopeandsparks.profile.controller;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.profile.dto.CollectionToggleRequest;
import com.hopeandsparks.profile.dto.UserProfileUpdateRequest;
import com.hopeandsparks.profile.service.ProfileService;
import com.hopeandsparks.profile.vo.CollectionItemVO;
import com.hopeandsparks.profile.vo.LearningStatsVO;
import com.hopeandsparks.profile.vo.UserHomeVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户中心聚合接口，负责学习统计和“我的收藏”等展示型接口。
 *
 * <p>收藏本身不归 profile 模块写入：资源收藏在 {@code arch-resource}，文章收藏在
 * {@code arch-community}。这里后续只做聚合查询，方便前端一个页面展示多类用户数据。</p>
 */
@RestController
public class UserCenterController {

    private final ProfileService profileService;

    public UserCenterController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/api/v1/users/{userId}")
    public ApiResponse<UserHomeVO> userHomepage(@PathVariable String userId) {
        return ApiResponse.ok(profileService.userHome(userId));
    }

    @PutMapping("/api/v1/user/profile")
    public ApiResponse<UserHomeVO> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return ApiResponse.ok("资料已更新", profileService.updateProfile(principal(authentication), request));
    }

    @GetMapping("/api/v1/user/learning-stats")
    public ApiResponse<LearningStatsVO> learningStats(Authentication authentication) {
        return ApiResponse.ok(profileService.learningStats(principal(authentication)));
    }

    @GetMapping("/api/v1/user/collections")
    public ApiResponse<PageResponse<CollectionItemVO>> collections(
            Authentication authentication,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.ok(profileService.collections(principal(authentication), type, page, pageSize));
    }

    @PostMapping("/api/v1/user/collections")
    public ApiResponse<Void> toggleCollection(
            Authentication authentication,
            @Valid @RequestBody CollectionToggleRequest request
    ) {
        profileService.toggleCollection(principal(authentication), request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/api/v1/user/collections/{collectionId}")
    public ApiResponse<Void> deleteCollection(Authentication authentication, @PathVariable String collectionId) {
        profileService.deleteCollection(principal(authentication), collectionId);
        return ApiResponse.ok(null);
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
