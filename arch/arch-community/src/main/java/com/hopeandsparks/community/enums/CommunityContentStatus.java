package com.hopeandsparks.community.enums;

import java.util.Arrays;

/**
 * Moderation status shared by article posts and comments.
 */
public enum CommunityContentStatus {
    DRAFT(0, "draft"),
    PUBLISHED(1, "published"),
    PENDING(2, "pending"),
    RISK(3, "risk"),
    BLOCKED(4, "blocked"),
    OFFLINE(5, "offline");

    private final int code;
    private final String apiValue;

    CommunityContentStatus(int code, String apiValue) {
        this.code = code;
        this.apiValue = apiValue;
    }

    public int code() {
        return code;
    }

    public String apiValue() {
        return apiValue;
    }

    public boolean publicVisible() {
        return this == PUBLISHED;
    }

    public boolean authorVisible() {
        return this == DRAFT || this == PENDING || this == RISK || this == BLOCKED || this == PUBLISHED;
    }

    public boolean canBeModerated() {
        return this == PENDING || this == RISK;
    }

    public static CommunityContentStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(PENDING);
    }

    public static CommunityContentStatus fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(status -> status.apiValue.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}
