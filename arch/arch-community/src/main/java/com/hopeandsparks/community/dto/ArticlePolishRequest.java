package com.hopeandsparks.community.dto;

import jakarta.validation.constraints.NotBlank;

public record ArticlePolishRequest(@NotBlank String content) {
}
