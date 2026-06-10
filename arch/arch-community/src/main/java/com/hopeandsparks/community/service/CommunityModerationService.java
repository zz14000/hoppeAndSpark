package com.hopeandsparks.community.service;

/**
 * Worker-facing entrypoints for community moderation queue messages.
 */
public interface CommunityModerationService {

    int consumePendingMessages();

    void moderate(String targetType, String targetId, String taskId);
}
