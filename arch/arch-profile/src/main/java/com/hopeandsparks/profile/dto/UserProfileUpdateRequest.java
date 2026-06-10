package com.hopeandsparks.profile.dto;

public record UserProfileUpdateRequest(
        String nickname,
        String avatarUrl,
        String learningGoal,
        String majorDomain,
        String gradeLevel
) {
}
