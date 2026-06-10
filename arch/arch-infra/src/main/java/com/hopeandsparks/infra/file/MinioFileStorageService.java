package com.hopeandsparks.infra.file;

import com.hopeandsparks.infra.config.InfraProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MinioFileStorageService implements FileStorageService {

    private final MinioClient client;
    private final InfraProperties.Minio properties;
    private final Map<String, StoredFileVO> files = new ConcurrentHashMap<>();

    public MinioFileStorageService(MinioClient client, InfraProperties.Minio properties) {
        this.client = client;
        this.properties = properties;
        ensureBucket();
    }

    @Override
    public UploadTokenVO createUploadToken(UploadTokenRequest request) {
        try {
            String fileId = UUID.randomUUID().toString();
            String objectKey = objectKey(fileId, request);
            int expiryMinutes = Math.max(1, properties.getPresignedExpiryMinutes());
            String uploadUrl = client.getPresignedObjectUrl(io.minio.GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
            return new UploadTokenVO(fileId, uploadUrl, objectKey, LocalDateTime.now().plusMinutes(expiryMinutes), false);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create MinIO upload token: " + exception.getMessage(), exception);
        }
    }

    @Override
    public StoredFileVO completeUpload(CompleteUploadRequest request) {
        try {
            String fileId = safe(request.fileId(), UUID.randomUUID().toString());
            String objectKey = safe(request.objectKey(), objectKey(fileId, new UploadTokenRequest(request.fileName(), request.fileType(), request.fileSize(), "unknown")));
            client.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
            StoredFileVO file = new StoredFileVO(
                    fileId,
                    safe(request.fileName(), fileId),
                    safe(request.fileType(), "application/octet-stream"),
                    request.fileSize() == null ? 0L : request.fileSize(),
                    objectKey,
                    publicUrl(objectKey),
                    false
            );
            files.put(fileId, file);
            return file;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to complete MinIO upload: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Optional<StoredFileVO> findByFileId(String fileId) {
        return Optional.ofNullable(files.get(fileId));
    }

    @Override
    public Optional<StoredFileObject> open(String fileId) {
        StoredFileVO file = files.get(fileId);
        if (file == null) {
            return Optional.empty();
        }
        try {
            InputStream inputStream = client.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(file.objectKey())
                    .build());
            return Optional.of(new StoredFileObject(file, inputStream, file.objectKey()));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to open MinIO object: " + exception.getMessage(), exception);
        }
    }

    private void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(properties.getBucket()).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to initialize MinIO bucket: " + exception.getMessage(), exception);
        }
    }

    private String objectKey(String fileId, UploadTokenRequest request) {
        String bizType = safe(request.bizType(), "default").replaceAll("[^a-zA-Z0-9._-]", "-");
        String fileName = safe(request.fileName(), "upload.bin").replaceAll("[\\\\/]+", "_");
        return bizType + "/" + fileId + "/" + fileName;
    }

    private String publicUrl(String objectKey) {
        String endpoint = properties.getPublicEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return endpoint + "/" + properties.getBucket() + "/" + objectKey;
    }

    private String safe(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
