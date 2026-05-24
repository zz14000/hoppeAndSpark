package com.hopeandsparks.manage.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "后台 Controller 资源")
public record AdminResourceVO(
        @Schema(description = "资源ID", example = "1")
        String id,

        @Schema(description = "资源名称", example = "数据看板资源")
        String name,

        @Schema(description = "资源标识", example = "manage:dashboard")
        String code,

        @Schema(description = "Controller 基础路径", example = "/api/v1/manage/dashboard")
        String url,

        @Schema(description = "Spring Security 动态权限标识", example = "1:数据看板资源")
        String authority
) {
}
