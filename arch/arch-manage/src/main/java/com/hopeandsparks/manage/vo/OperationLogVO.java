package com.hopeandsparks.manage.vo;

import java.time.LocalDateTime;

/**
 * Manage operation log response.
 */
public record OperationLogVO(
        String id,
        String adminId,
        String adminUsername,
        String moduleName,
        String actionType,
        String targetType,
        String targetId,
        String detail,
        String ipAddress,
        LocalDateTime createdAt
) {
}
