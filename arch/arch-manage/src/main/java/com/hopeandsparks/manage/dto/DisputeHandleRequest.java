package com.hopeandsparks.manage.dto;

/**
 * Request for reviewing an AI dispute ticket.
 */
public record DisputeHandleRequest(
        String status,
        String reviewResult,
        String result,
        String remark,
        String note,
        String correctedContent,
        String reparseDocumentId
) {
}
