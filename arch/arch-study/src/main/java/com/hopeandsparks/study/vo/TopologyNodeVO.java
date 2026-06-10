package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Study topology node")
public record TopologyNodeVO(
        String id,
        String name,
        String desc,
        String knowledgeKey,
        String status,
        Integer progress,
        List<String> prerequisites,
        String resourceNetworkUrl,
        String resourceNetworkApi,
        ClickActionVO clickAction
) {
}
