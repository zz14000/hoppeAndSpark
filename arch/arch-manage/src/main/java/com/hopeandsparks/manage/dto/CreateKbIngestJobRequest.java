package com.hopeandsparks.manage.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateKbIngestJobRequest(
        @NotBlank(message = "fileId不能为空")
        String fileId,
        @NotBlank(message = "title不能为空")
        String title,
        @NotBlank(message = "domain不能为空")
        String domain,
        @NotBlank(message = "projectId不能为空")
        String projectId,
        String collection,
        String sourceType,
        String parseStrategy,
        Boolean parseNow
) {
}
