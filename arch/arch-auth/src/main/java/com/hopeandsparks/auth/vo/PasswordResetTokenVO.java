package com.hopeandsparks.auth.vo;

public record PasswordResetTokenVO(String resetToken, long expiresInSeconds) {
}
