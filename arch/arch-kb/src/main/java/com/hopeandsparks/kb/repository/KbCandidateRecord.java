package com.hopeandsparks.kb.repository;

import java.time.LocalDateTime;

public record KbCandidateRecord(
        String candidateId,
        String documentId,
        String tenantUserId,
        String projectId,
        String sourceUrl,
        String sourceDomain,
        String sourceTitle,
        LocalDateTime fetchTime,
        double rerankScore,
        double retrievalScore,
        int contentLength,
        String dedupeHash,
        String governanceStatus,
        String promotionStatus,
        String promotionReason,
        String reviewerId,
        String reviewComment,
        String approvedDocumentId,
        LocalDateTime rolledBackAt,
        String contentText
) {
}
