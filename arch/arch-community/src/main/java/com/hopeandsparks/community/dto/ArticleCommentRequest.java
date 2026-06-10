package com.hopeandsparks.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * Request body for posting a comment under an article.
 */
public record ArticleCommentRequest(
        @NotBlank(message = "content cannot be blank")
        @Size(max = 1000, message = "content cannot exceed 1000 characters")
        String content,
        String parentId,
        List<Map<String, Object>> attachments
) {
}
