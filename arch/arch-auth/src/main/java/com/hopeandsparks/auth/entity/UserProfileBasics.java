package com.hopeandsparks.auth.entity;

public record UserProfileBasics(
        Long id,
        String majorDomain,
        String gradeLevel,
        String knowledgeBaseLevel,
        String learningGoal,
        String selfDiscipline,
        String currentWeakness
) {
}
