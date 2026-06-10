package com.hopeandsparks.study.entity;

import java.math.BigDecimal;

/**
 * User progress for one knowledge node.
 */
public record UserKnowledgeProgress(
        Long nodeId,
        String progressStatus,
        Integer progressPercent,
        BigDecimal masteryScore
) {
}
