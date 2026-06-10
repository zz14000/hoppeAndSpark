package com.hopeandsparks.manage.entity;

import java.time.LocalDateTime;

/**
 * Read model for feedback_ticket.
 */
public record FeedbackTicket(
        Long id,
        Long userId,
        String username,
        String nickname,
        String targetType,
        Long targetId,
        String issueType,
        String description,
        String snapshotContent,
        String status,
        Long adminId,
        String adminUsername,
        String processRemark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
