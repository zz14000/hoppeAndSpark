package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource network center knowledge")
public record ResourceNetworkKnowledgeVO(
        String id,
        String name,
        String englishName,
        String chapter,
        String status,
        Integer progress,
        String description
) {
}
