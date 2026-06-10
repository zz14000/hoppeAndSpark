package com.hopeandsparks.kb.dto;

import jakarta.validation.constraints.NotBlank;

public record KbDocumentCreateRequest(
        @NotBlank(message = "文档标题不能为空")
        String title,
        String domain,
        String projectId,
        String fileId,
        String sourceType,
        String sourceUrl,
        String contentText,
        String collection,
        Boolean parseNow
) {
}
