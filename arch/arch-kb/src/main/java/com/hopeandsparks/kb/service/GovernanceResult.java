package com.hopeandsparks.kb.service;

public record GovernanceResult(
        String promotionStatus,
        String approvedDocumentId,
        String reason,
        boolean retryable
) {
}
