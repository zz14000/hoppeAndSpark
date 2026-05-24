package com.hopeandsparks.manage.entity;

public record AdminAccount(
        Long id,
        String username,
        String realName,
        String passwordHash,
        Integer adminStatus
) {
}
