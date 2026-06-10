package com.hopeandsparks.infra.file;

import java.time.LocalDateTime;

/**
 * 文件元数据响应。
 * 后续接入 sys_oss_file 时，这些字段可以直接从数据库读取。
 */
public record StoredFileVO(
        String fileId,
        String originalFileName,
        String contentType,
        Long fileSize,
        String businessType,
        String objectKey,
        String publicUrl,
        String storageStatus,
        LocalDateTime createdAt
) {
}
