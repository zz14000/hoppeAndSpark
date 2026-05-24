package com.hopeandsparks.infra.security;

/**
 * Principal stored in Spring Security after a JWT is accepted.
 */
public record AuthenticatedPrincipal(
        Long id,
        String username,
        IdentityType type,
        String sessionToken
) {
}
