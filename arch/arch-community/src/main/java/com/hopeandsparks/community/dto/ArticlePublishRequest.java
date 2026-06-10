package com.hopeandsparks.community.dto;

import jakarta.validation.constraints.NotBlank;

public record ArticlePublishRequest(@NotBlank String title, String content) {
}
