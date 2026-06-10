package com.hopeandsparks.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "修改密码请求")
public record UserChangePasswordRequest(
        @Schema(description = "旧密码")
        @NotBlank(message = "旧密码不能为空")
        String oldPassword,

        @Schema(description = "新密码")
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 64, message = "新密码长度需要在6到64位之间")
        String newPassword
) {
}
