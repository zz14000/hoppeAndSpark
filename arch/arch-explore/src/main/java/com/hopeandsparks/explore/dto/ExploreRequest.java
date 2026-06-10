package com.hopeandsparks.explore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Map;

@Schema(description = "Nebula 探索请求")
public record ExploreRequest(
        @Schema(description = "搜索关键词，兼容 API 文档字段")
        String keyword,

        @Schema(description = "搜索问题，兼容前端样例字段")
        String query,

        @Schema(description = "学习领域")
        String domain,

        @Schema(description = "探索模式，如 quick/deep")
        String mode,

        @Schema(description = "探索深度")
        @Min(value = 1, message = "depth 至少为1")
        @Max(value = 5, message = "depth 不能超过5")
        Integer depth,

        @Schema(description = "学习目标")
        List<String> goals,

        @Schema(description = "偏好的资源类型")
        List<String> preferredResourceTypes,

        @Schema(description = "兼容旧字段 preferredTypes")
        List<String> preferredTypes,

        @Schema(description = "页面上下文")
        Map<String, Object> context
) {
}
