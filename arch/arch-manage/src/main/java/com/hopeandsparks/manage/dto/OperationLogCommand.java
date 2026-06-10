package com.hopeandsparks.manage.dto;

/**
 * Command for writing a manage operation log.
 */
public record OperationLogCommand(
        Long adminId,
        String moduleName,
        String actionType,
        String targetType,
        Long targetId,
        String detail,
        String ipAddress
) {
}
