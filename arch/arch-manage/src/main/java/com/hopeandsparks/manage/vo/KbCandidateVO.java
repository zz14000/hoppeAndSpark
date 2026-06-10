package com.hopeandsparks.manage.vo;

public record KbCandidateVO(
        String candidateId,
        String documentId,
        String tenantUserId,
        String projectId,
        String sourceUrl,
        String sourceDomain,
        String sourceTitle,
        double rerankScore,
        double retrievalScore,
        int contentLength,
        String governanceStatus,
        String promotionStatus,
        String promotionReason,
        String approvedDocumentId
) {
}
