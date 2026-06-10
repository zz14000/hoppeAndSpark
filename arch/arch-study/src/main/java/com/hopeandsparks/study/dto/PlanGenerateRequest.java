package com.hopeandsparks.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.util.List;

@Schema(description = "Generate study plan request")
public record PlanGenerateRequest(
        @Schema(description = "Course ID")
        String courseId,

        @Schema(description = "Course domain keyword")
        String domain,

        @Schema(description = "Learning goal")
        String goal,

        @Schema(description = "Deadline date, yyyy-MM-dd")
        String deadline,

        @Schema(description = "Difficulty")
        String difficulty,

        @Schema(description = "Daily study minutes")
        @Min(1)
        Integer dailyMinutes,

        @Schema(description = "Preferred time slots")
        List<String> preferredTimeSlots
) {
}
