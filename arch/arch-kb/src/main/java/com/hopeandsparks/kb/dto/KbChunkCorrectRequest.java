package com.hopeandsparks.kb.dto;

import jakarta.validation.constraints.NotBlank;

public record KbChunkCorrectRequest(
        @NotBlank(message = "切片内容不能为空")
        String contentText,
        String reason
) {
}
