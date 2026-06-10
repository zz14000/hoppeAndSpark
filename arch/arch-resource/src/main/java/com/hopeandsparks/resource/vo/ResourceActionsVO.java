package com.hopeandsparks.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Available actions for a resource")
public record ResourceActionsVO(
        boolean continueLearning,
        boolean collect,
        boolean export,
        boolean feedback
) {
}
