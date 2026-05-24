package com.hopeandsparks.auth.entity;

import java.time.LocalDateTime;

public record UserLoginSession(
        Long id,
        Long userId,
        String sessionToken,
        LocalDateTime expiresAt,
        Integer sessionStatus
) {
}
