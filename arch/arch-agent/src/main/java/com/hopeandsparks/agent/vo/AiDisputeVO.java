package com.hopeandsparks.agent.vo;

import java.time.LocalDateTime;

public record AiDisputeVO(
        String disputeId,
        String status,
        String reason,
        LocalDateTime createdAt,
        boolean mock
) {
}
