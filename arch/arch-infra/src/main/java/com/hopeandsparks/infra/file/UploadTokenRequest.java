package com.hopeandsparks.infra.file;

public record UploadTokenRequest(
        String fileName,
        String fileType,
        Long fileSize,
        String bizType
) {
}
