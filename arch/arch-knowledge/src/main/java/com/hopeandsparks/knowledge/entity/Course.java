package com.hopeandsparks.knowledge.entity;

/**
 * Course master data from the course table.
 */
public record Course(
        Long id,
        String courseCode,
        String courseName,
        String courseDesc,
        String majorDomain,
        String difficultyLevel
) {
}
