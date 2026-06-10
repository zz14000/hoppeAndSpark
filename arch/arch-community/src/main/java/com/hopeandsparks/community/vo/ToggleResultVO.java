package com.hopeandsparks.community.vo;

/**
 * Common result for like and collect toggles.
 */
public record ToggleResultVO(
        String targetId,
        String action,
        boolean active,
        int count
) {
}
