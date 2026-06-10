package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Resource node in network")
public record ResourceNetworkResourceNodeVO(
        String id,
        String categoryId,
        String type,
        String title,
        String summary,
        Integer durationSeconds,
        String status,
        Map<String, Object> target
) {
}
