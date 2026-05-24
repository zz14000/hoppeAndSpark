package com.hopeandsparks.manage.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "后台管理员认证响应")
public record AdminAuthTokenVO(
        @Schema(description = "JWT access token")
        String accessToken,

        @Schema(description = "请求头 token 类型", example = "Bearer")
        String tokenType,

        @Schema(description = "过期秒数", example = "43200")
        long expiresIn,

        @Schema(description = "管理员资料")
        AdminProfileVO admin,

        @Schema(description = "后台菜单权限")
        List<AdminMenuVO> menus,

        @Schema(description = "后台 Controller 资源权限")
        List<AdminResourceVO> resources
) {
}
