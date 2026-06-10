package com.hopeandsparks.explore.dto;

import jakarta.validation.constraints.NotBlank;

public record ExploreRequest(
        @NotBlank(message = "探索主题不能为空")
        String topic,
        String courseName
) {
}
