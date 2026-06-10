package com.hopeandsparks.profile.service.impl;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.profile.dto.CollectionToggleRequest;
import com.hopeandsparks.profile.dto.OnboardingAnswerRequest;
import com.hopeandsparks.profile.dto.OnboardingCompleteRequest;
import com.hopeandsparks.profile.dto.SparkProfileRebuildRequest;
import com.hopeandsparks.profile.dto.UserProfileUpdateRequest;
import com.hopeandsparks.profile.service.ProfileService;
import com.hopeandsparks.profile.vo.CollectionItemVO;
import com.hopeandsparks.profile.vo.LearningStatsVO;
import com.hopeandsparks.profile.vo.OnboardingAnswerVO;
import com.hopeandsparks.profile.vo.OnboardingQuestionVO;
import com.hopeandsparks.profile.vo.SparkProfileVO;
import com.hopeandsparks.profile.vo.UserHomeVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Override
    public List<OnboardingQuestionVO> questions() {
        return List.of(new OnboardingQuestionVO("goal", "你的学习目标是什么?", "text", List.of()));
    }

    @Override
    public OnboardingAnswerVO answer(AuthenticatedPrincipal principal, OnboardingAnswerRequest request) {
        return new OnboardingAnswerVO(request.questionId(), true, "mock accepted");
    }

    @Override
    public SparkProfileVO complete(AuthenticatedPrincipal principal, OnboardingCompleteRequest request) {
        return profile(principal, request == null ? "" : request.majorDomain(), request == null ? "" : request.goal());
    }

    @Override
    public SparkProfileVO rebuild(AuthenticatedPrincipal principal, SparkProfileRebuildRequest request) {
        return profile(principal, "", "rebuild: " + request.reason());
    }

    @Override
    public UserHomeVO userHome(String userId) {
        return new UserHomeVO(userId, "mock user", "", "mock profile", true);
    }

    @Override
    public UserHomeVO updateProfile(AuthenticatedPrincipal principal, UserProfileUpdateRequest request) {
        return new UserHomeVO(userId(principal), request.nickname(), request.avatarUrl(), request.learningGoal(), true);
    }

    @Override
    public LearningStatsVO learningStats(AuthenticatedPrincipal principal) {
        return new LearningStatsVO(1, 0, 0, true);
    }

    @Override
    public PageResponse<CollectionItemVO> collections(AuthenticatedPrincipal principal, String type, long page, long pageSize) {
        return PageResponse.of(page, pageSize, 0, List.of());
    }

    @Override
    public void toggleCollection(AuthenticatedPrincipal principal, CollectionToggleRequest request) {
    }

    @Override
    public void deleteCollection(AuthenticatedPrincipal principal, String collectionId) {
    }

    private SparkProfileVO profile(AuthenticatedPrincipal principal, String major, String goal) {
        return new SparkProfileVO(userId(principal), major == null || major.isBlank() ? "未设置" : major,
                goal == null || goal.isBlank() ? "未设置" : goal, "beginner", List.of(), true);
    }

    private String userId(AuthenticatedPrincipal principal) {
        return principal == null || principal.id() == null ? "anonymous" : String.valueOf(principal.id());
    }
}
