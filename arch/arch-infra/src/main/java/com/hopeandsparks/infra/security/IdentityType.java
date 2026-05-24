package com.hopeandsparks.infra.security;

/**
 * Token identity boundary. Frontend users and Manage admins are deliberately separate principals.
 */
public enum IdentityType {
    USER,
    ADMIN
}
