package com.hopeandsparks.explore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "生成思维导图请求")
public record MindMapRequest(
        @Schema(description = "导图样式")
        String style,

        @Schema(description = "导图深度")
        @Min(value = 1, message = "depth 至少为1")
        @Max(value = 5, message = "depth 不能超过5")
        Integer depth,

        @Schema(description = "是否包含资源节点")
        Boolean includeResources
) {
}
