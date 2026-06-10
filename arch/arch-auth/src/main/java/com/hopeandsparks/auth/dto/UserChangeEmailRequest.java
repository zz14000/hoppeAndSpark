package com.hopeandsparks.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "绑定或更换邮箱请求")
public record UserChangeEmailRequest(
        @Schema(description = "新邮箱", example = "new-spark@example.com")
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱不能超过100个字符")
        String email,

        @Schema(description = "验证码，W2 先做占位校验", example = "123456")
        @NotBlank(message = "验证码不能为空")
        String code,

        @Schema(description = "当前密码，可选；传入时会校验")
        String password
) {
}
