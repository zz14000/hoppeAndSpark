package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Frontend click action")
public record ClickActionVO(
        String type,
        String target,
        Map<String, String> params
) {
}
