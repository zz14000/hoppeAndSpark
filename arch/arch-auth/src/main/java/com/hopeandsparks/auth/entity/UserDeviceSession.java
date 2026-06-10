package com.hopeandsparks.auth.entity;

import java.time.LocalDateTime;

public record UserDeviceSession(
        Long id,
        Long userId,
        String sessionToken,
        String deviceId,
        String deviceName,
        String clientType,
        String ipAddress,
        LocalDateTime lastActiveAt,
        LocalDateTime expiresAt,
        Integer sessionStatus
) {
}
