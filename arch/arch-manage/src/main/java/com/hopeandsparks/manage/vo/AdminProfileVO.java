package com.hopeandsparks.manage.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "后台管理员资料")
public record AdminProfileVO(
        @Schema(description = "管理员ID", example = "1")
        String id,

        @Schema(description = "管理员账号", example = "admin")
        String username,

        @Schema(description = "真实姓名", example = "系统管理员")
        String realName,

        @Schema(description = "角色标识")
        List<String> roles
) {
}
