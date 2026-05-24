package com.hopeandsparks.infra.security;

/**
 * Stable JWT payload fields used by both Spark frontend users and Manage admins.
 */
public record JwtSubject(
        Long id,
        String username,
        IdentityType type,
        String sessionToken
) {
}
