package com.hopeandsparks.infra.file;

public record StoredFileVO(
        String fileId,
        String fileName,
        String fileType,
        Long fileSize,
        String objectKey,
        String url,
        boolean mock
) {
}
