package com.hopeandsparks.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request body used when a user submits an article for moderation.
 */
public record ArticlePublishRequest(
        String draftId,
        @NotBlank(message = "title cannot be blank")
        @Size(max = 150, message = "title cannot exceed 150 characters")
        String title,
        @NotBlank(message = "content cannot be blank")
        String content,
        @Size(max = 500, message = "summary cannot exceed 500 characters")
        String summary,
        List<String> tags,
        String category,
        String coverFileId,
        String coverUrl,
        String visibility
) {
}
