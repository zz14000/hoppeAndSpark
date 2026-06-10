package com.hopeandsparks.community.dto;

import jakarta.validation.constraints.NotBlank;

public record ArticleDraftRequest(@NotBlank String title, String content) {
}
