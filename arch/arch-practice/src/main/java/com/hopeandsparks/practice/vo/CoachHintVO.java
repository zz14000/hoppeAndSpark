package com.hopeandsparks.practice.vo;

import java.util.List;
import java.util.Map;

/**
 * Coach 提示结果，保留标准 Agent 输出协议字段。
 */
public record CoachHintVO(
        String taskId,
        String sourceAgent,
        String taskType,
        String status,
        String errorType,
        String diagnosis,
        String hintLevel1,
        String hintLevel2,
        String fullExplanation,
        String nextStep,
        List<String> knowledgePoints,
        String encouragement,
        Map<String, Object> payload
) {
}
