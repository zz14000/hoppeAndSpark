package com.hopeandsparks.auth.entity;

import java.time.LocalDateTime;

public record UserAccount(
        Long id,
        String username,
        String nickname,
        String passwordHash,
        String avatarUrl,
        String phone,
        String email,
        Integer accountStatus,
        String banReason,
        LocalDateTime banUntil
) {
}
