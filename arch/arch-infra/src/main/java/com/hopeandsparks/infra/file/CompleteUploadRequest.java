package com.hopeandsparks.infra.file;

public record CompleteUploadRequest(
        String fileId,
        String fileHash,
        String objectKey,
        String fileName,
        String fileType,
        Long fileSize
) {
}
