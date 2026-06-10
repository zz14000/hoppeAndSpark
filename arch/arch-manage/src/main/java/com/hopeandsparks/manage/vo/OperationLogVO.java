package com.hopeandsparks.manage.vo;

import java.time.LocalDateTime;

public record OperationLogVO(
        String id,
        String module,
        String action,
        String targetType,
        Long targetId,
        String detail,
        LocalDateTime createdAt,
        boolean mock
) {
}
