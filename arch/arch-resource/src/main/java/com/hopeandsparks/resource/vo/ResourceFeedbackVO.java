package com.hopeandsparks.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource feedback result")
public record ResourceFeedbackVO(
        String ticketId,
        String status
) {
}
