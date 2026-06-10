package com.hopeandsparks.infra.file;

/**
 * 上传完成回调请求。
 * fileId 来自 createUploadToken，其他字段允许前端再次确认。
 */
public record CompleteUploadRequest(
        String fileId,
        String originalFileName,
        String contentType,
        Long fileSize,
        String businessType
) {
}
