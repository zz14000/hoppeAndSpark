package com.hopeandsparks.profile.dto;

import jakarta.validation.constraints.NotBlank;

public record SparkProfileRebuildRequest(
        @NotBlank(message = "重建原因不能为空")
        String reason
) {
}
