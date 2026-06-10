package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Study topology edge")
public record TopologyEdgeVO(
        String from,
        String to,
        String type
) {
}
