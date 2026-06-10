package com.hopeandsparks.manage.entity;

import java.time.LocalDateTime;

/**
 * Read model for sys_operation_log.
 */
public record OperationLog(
        Long id,
        Long adminId,
        String adminUsername,
        String moduleName,
        String actionType,
        String targetType,
        Long targetId,
        String detail,
        String ipAddress,
        LocalDateTime createdAt
) {
}
