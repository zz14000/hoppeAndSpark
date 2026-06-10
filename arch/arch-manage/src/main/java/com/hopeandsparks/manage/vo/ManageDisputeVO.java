package com.hopeandsparks.manage.vo;

import java.time.LocalDateTime;

/**
 * Manage dispute ticket response.
 */
public record ManageDisputeVO(
        String id,
        String userId,
        String username,
        String nickname,
        String targetType,
        String targetId,
        String issueType,
        String description,
        String snapshotContent,
        String status,
        String adminId,
        String adminUsername,
        String processRemark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
