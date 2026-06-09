package com.hopeandsparks.community.consumer;

import com.hopeandsparks.community.service.CommunityModerationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Consumer boundary for article and comment moderation messages.
 */
@Component
public class CommunityModerationConsumer {

    private final CommunityModerationService communityModerationService;

    public CommunityModerationConsumer(CommunityModerationService communityModerationService) {
        this.communityModerationService = communityModerationService;
    }

    @Scheduled(
            fixedDelayString = "${hope.community.moderation.fixed-delay-ms:5000}",
            initialDelayString = "${hope.community.moderation.initial-delay-ms:5000}"
    )
    public void consumeScheduled() {
        communityModerationService.consumePendingMessages();
    }

    /**
     * Manual hook for tests and demos when waiting for the scheduler is inconvenient.
     */
    public int consumeOnce() {
        return communityModerationService.consumePendingMessages();
    }
}
