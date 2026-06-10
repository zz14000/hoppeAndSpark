package com.hopeandsparks.infra.file;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MockFileStorageService implements FileStorageService {

    private final Map<String, StoredFileVO> files = new ConcurrentHashMap<>();

    @Override
    public UploadTokenVO createUploadToken(UploadTokenRequest request) {
        String fileId = "mock-file-" + System.currentTimeMillis();
        String objectKey = "mock/" + fileId + "/" + safe(request.fileName(), "upload.bin");
        return new UploadTokenVO(fileId, "mock://upload/" + fileId, objectKey, LocalDateTime.now().plusMinutes(30), true);
    }

    @Override
    public StoredFileVO completeUpload(CompleteUploadRequest request) {
        String fileId = safe(request.fileId(), "mock-file-" + System.currentTimeMillis());
        StoredFileVO file = new StoredFileVO(
                fileId,
                safe(request.fileName(), fileId),
                safe(request.fileType(), "application/octet-stream"),
                request.fileSize() == null ? 0L : request.fileSize(),
                "mock/" + fileId,
                "mock://file/" + fileId,
                true
        );
        files.put(fileId, file);
        return file;
    }

    @Override
    public Optional<StoredFileVO> findByFileId(String fileId) {
        return Optional.ofNullable(files.get(fileId));
    }

    @Override
    public Optional<StoredFileObject> open(String fileId) {
        return Optional.empty();
    }

    private String safe(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
