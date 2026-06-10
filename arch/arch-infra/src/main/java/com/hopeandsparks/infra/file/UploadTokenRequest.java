package com.hopeandsparks.infra.file;

/**
 * 申请上传凭证时前端提交的文件信息。
 * 这里只做基础字段，真实 MinIO 签名参数后面再补。
 */
public record UploadTokenRequest(
        String originalFileName,
        String contentType,
        Long fileSize,
        String businessType
) {
}
