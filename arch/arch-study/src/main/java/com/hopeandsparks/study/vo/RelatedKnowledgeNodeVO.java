package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Related knowledge node")
public record RelatedKnowledgeNodeVO(
        String id,
        String name,
        String relation,
        Map<String, Object> target
) {
}
