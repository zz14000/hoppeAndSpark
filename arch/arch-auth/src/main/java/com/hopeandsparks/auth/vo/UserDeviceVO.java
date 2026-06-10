package com.hopeandsparks.auth.vo;

import java.time.LocalDateTime;

public record UserDeviceVO(
        String sessionId,
        String deviceId,
        String deviceName,
        String clientType,
        String ipAddress,
        LocalDateTime lastActiveAt,
        LocalDateTime expiresAt,
        boolean current
) {
}
