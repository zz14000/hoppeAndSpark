package com.hopeandsparks.infra.file;

/**
 * 上传凭证响应。
 * mock 阶段 uploadUrl 只是占位地址，前端可以先按字段联调。
 */
public record UploadTokenVO(
        String fileId,
        String objectKey,
        String uploadUrl,
        String publicUrl,
        long expireSeconds
) {
}
