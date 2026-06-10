package com.hopeandsparks.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for the mock Horizon polishing helper.
 */
public record ArticlePolishRequest(
        @Size(max = 150, message = "title cannot exceed 150 characters")
        String title,
        @NotBlank(message = "content cannot be blank")
        String content,
        String tone,
        String agentId
) {
}
