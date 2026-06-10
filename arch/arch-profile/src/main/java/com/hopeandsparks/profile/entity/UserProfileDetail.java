package com.hopeandsparks.profile.entity;

public record UserProfileDetail(
        Long userId,
        String username,
        String nickname,
        String avatarUrl,
        Long profileId,
        String majorDomain,
        String gradeLevel,
        String knowledgeBaseLevel,
        String cognitiveStyle,
        String learningPreference,
        String learningGoal,
        String selfDiscipline,
        String currentWeakness
) {
}
