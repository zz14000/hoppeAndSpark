package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Study topology")
public record TopologyVO(
        String planId,
        List<TopologyNodeVO> nodes,
        List<TopologyEdgeVO> edges
) {
}
