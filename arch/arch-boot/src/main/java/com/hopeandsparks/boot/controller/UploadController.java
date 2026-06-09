package com.hopeandsparks.boot.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.infra.file.CompleteUploadRequest;
import com.hopeandsparks.infra.file.FileStorageService;
import com.hopeandsparks.infra.file.StoredFileVO;
import com.hopeandsparks.infra.file.UploadTokenRequest;
import com.hopeandsparks.infra.file.UploadTokenVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全局上传入口。
 * Controller 只负责接收请求并委托给 arch-infra，文件归属由后续业务模块保存。
 */
@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/token")
    public ApiResponse<UploadTokenVO> createUploadToken(@RequestBody(required = false) UploadTokenRequest request) {
        return ApiResponse.ok(fileStorageService.createUploadToken(request));
    }

    @PostMapping("/complete")
    public ApiResponse<StoredFileVO> completeUpload(@RequestBody(required = false) CompleteUploadRequest request) {
        return ApiResponse.ok(fileStorageService.completeUpload(request));
    }
}
