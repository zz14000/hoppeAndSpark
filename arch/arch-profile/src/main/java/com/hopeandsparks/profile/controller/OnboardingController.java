package com.hopeandsparks.profile.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.profile.dto.OnboardingAnswerRequest;
import com.hopeandsparks.profile.dto.OnboardingCompleteRequest;
import com.hopeandsparks.profile.dto.SparkProfileRebuildRequest;
import com.hopeandsparks.profile.service.ProfileService;
import com.hopeandsparks.profile.vo.OnboardingAnswerVO;
import com.hopeandsparks.profile.vo.OnboardingQuestionVO;
import com.hopeandsparks.profile.vo.SparkProfileVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Spark 画像引导接口，负责画像问题、单轮回答、画像完成和重新构建画像。
 *
 * <p>后续实现时会调用画像 Service，结合用户回答、规则和 Agent 能力生成 {@code user_profile}，
 * 并把长期偏好、目标、薄弱点等动态信息沉淀到记忆表中。</p>
 */
@RestController
public class OnboardingController {

    private final ProfileService profileService;

    public OnboardingController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/api/v1/onboarding/questions")
    public ApiResponse<List<OnboardingQuestionVO>> questions() {
        return ApiResponse.ok(profileService.questions());
    }

    @PostMapping("/api/v1/onboarding/answers")
    public ApiResponse<OnboardingAnswerVO> answer(
            Authentication authentication,
            @Valid @RequestBody OnboardingAnswerRequest request
    ) {
        return ApiResponse.ok(profileService.answer(principal(authentication), request));
    }

    @PostMapping("/api/v1/onboarding/complete")
    public ApiResponse<SparkProfileVO> complete(
            Authentication authentication,
            @RequestBody(required = false) OnboardingCompleteRequest request
    ) {
        return ApiResponse.ok("画像生成成功", profileService.complete(principal(authentication), request));
    }

    @PostMapping("/api/v1/spark-profile/rebuild")
    public ApiResponse<SparkProfileVO> rebuild(
            Authentication authentication,
            @Valid @RequestBody SparkProfileRebuildRequest request
    ) {
        return ApiResponse.ok("画像重建成功", profileService.rebuild(principal(authentication), request));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
