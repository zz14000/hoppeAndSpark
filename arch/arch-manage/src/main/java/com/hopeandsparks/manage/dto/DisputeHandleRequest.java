package com.hopeandsparks.manage.dto;

import jakarta.validation.constraints.NotBlank;

public record DisputeHandleRequest(
        @NotBlank(message = "处理状态不能为空")
        String status,
        String remark
) {
}
