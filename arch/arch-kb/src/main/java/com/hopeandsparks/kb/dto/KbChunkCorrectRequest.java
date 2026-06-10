package com.hopeandsparks.kb.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Manual correction request for a disputed KB chunk.
 */
public record KbChunkCorrectRequest(
        @NotBlank String correctedContent,
        String reason
) {
}
