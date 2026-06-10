package com.hopeandsparks.community.dto;

import jakarta.validation.constraints.NotBlank;

public record ArticleCommentRequest(@NotBlank String content, String parentId) {
}
