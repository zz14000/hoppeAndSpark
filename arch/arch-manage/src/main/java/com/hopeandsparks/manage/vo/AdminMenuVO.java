package com.hopeandsparks.manage.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "后台菜单")
public record AdminMenuVO(
        @Schema(description = "菜单ID", example = "1")
        String id,

        @Schema(description = "父菜单ID，0 表示根节点", example = "0")
        String parentId,

        @Schema(description = "菜单名称", example = "数据看板")
        String name,

        @Schema(description = "前端路由", example = "/manage/dashboard")
        String path,

        @Schema(description = "层级", example = "1")
        Integer level,

        @Schema(description = "排序值", example = "10")
        Integer sortOrder
) {
}
