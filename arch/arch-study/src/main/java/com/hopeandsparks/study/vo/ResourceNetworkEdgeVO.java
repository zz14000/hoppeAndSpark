package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource network edge")
public record ResourceNetworkEdgeVO(
        String from,
        String to,
        String type
) {
}
