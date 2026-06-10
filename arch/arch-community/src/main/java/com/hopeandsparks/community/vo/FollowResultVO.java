package com.hopeandsparks.community.vo;

/**
 * Follow or unfollow operation result.
 */
public record FollowResultVO(
        String userId,
        boolean followed
) {
}
