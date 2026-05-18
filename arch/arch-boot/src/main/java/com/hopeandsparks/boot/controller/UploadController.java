package com.hopeandsparks.boot.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 全局上传入口，负责提供上传凭证和上传完成回调。
 *
 * <p>按照架构约定，文件能力放在 {@code arch-infra}，业务模块只保存 fileId 或业务归属。
 * 当前 Controller 先占住 API 文档里的上传路径，后续会调用 infra 的文件 Service 生成 MinIO
 * 预签名地址，并在上传完成后写入 {@code sys_oss_file}。</p>
 */
@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    @PostMapping("/token")
    public ApiResponse<Map<String, Object>> createUploadToken(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("boot", "createUploadToken", values("request", request)));
    }

    @PostMapping("/complete")
    public ApiResponse<Map<String, Object>> completeUpload(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("boot", "completeUpload", values("request", request)));
    }
}
