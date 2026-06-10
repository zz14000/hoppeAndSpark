package com.hopeandsparks.infra.tool;

import java.time.LocalDateTime;

public record ToolCallRecord(
        String toolName,
        String inputSummary,
        String outputSummary,
        long durationMs,
        boolean success,
        String failureReason,
        LocalDateTime calledAt
) {
}
