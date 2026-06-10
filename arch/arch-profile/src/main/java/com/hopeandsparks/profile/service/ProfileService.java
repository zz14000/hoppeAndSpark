package com.hopeandsparks.profile.service;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.profile.dto.CollectionToggleRequest;
import com.hopeandsparks.profile.dto.OnboardingAnswerRequest;
import com.hopeandsparks.profile.dto.OnboardingCompleteRequest;
import com.hopeandsparks.profile.dto.SparkProfileRebuildRequest;
import com.hopeandsparks.profile.dto.UserProfileUpdateRequest;
import com.hopeandsparks.profile.vo.CollectionItemVO;
import com.hopeandsparks.profile.vo.LearningStatsVO;
import com.hopeandsparks.profile.vo.OnboardingAnswerVO;
import com.hopeandsparks.profile.vo.OnboardingQuestionVO;
import com.hopeandsparks.profile.vo.SparkProfileVO;
import com.hopeandsparks.profile.vo.UserHomeVO;

import java.util.List;

public interface ProfileService {

    List<OnboardingQuestionVO> questions();

    OnboardingAnswerVO answer(AuthenticatedPrincipal principal, OnboardingAnswerRequest request);

    SparkProfileVO complete(AuthenticatedPrincipal principal, OnboardingCompleteRequest request);

    SparkProfileVO rebuild(AuthenticatedPrincipal principal, SparkProfileRebuildRequest request);

    UserHomeVO userHome(String userId);

    UserHomeVO updateProfile(AuthenticatedPrincipal principal, UserProfileUpdateRequest request);

    LearningStatsVO learningStats(AuthenticatedPrincipal principal);

    PageResponse<CollectionItemVO> collections(AuthenticatedPrincipal principal, String type, long page, long pageSize);

    void toggleCollection(AuthenticatedPrincipal principal, CollectionToggleRequest request);

    void deleteCollection(AuthenticatedPrincipal principal, String collectionId);
}
