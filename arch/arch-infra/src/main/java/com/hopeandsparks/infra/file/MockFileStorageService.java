package com.hopeandsparks.infra.file;

import com.hopeandsparks.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件存储的 mock 实现。
 * 它不真的上传文件，只生成 fileId、objectKey 和访问地址，方便 W1/W2 先联调接口。
 */
@Service
public class MockFileStorageService implements FileStorageService {

    private static final long EXPIRE_SECONDS = 1800L;

    private final Map<String, StoredFileVO> files = new ConcurrentHashMap<>();
    private final String publicBaseUrl;

    public MockFileStorageService(@Value("${hope.file.mock-public-base-url:http://localhost:8080/mock-files}") String publicBaseUrl) {
        this.publicBaseUrl = removeLastSlash(publicBaseUrl);
    }

    @Override
    public UploadTokenVO createUploadToken(UploadTokenRequest request) {
        String fileId = "file_" + UUID.randomUUID().toString().replace("-", "");
        String fileName = cleanFileName(request == null ? null : request.originalFileName());
        String objectKey = buildObjectKey(fileId, fileName);
        String publicUrl = publicBaseUrl + "/" + objectKey;

        StoredFileVO draft = new StoredFileVO(
                fileId,
                fileName,
                request == null ? null : request.contentType(),
                request == null ? null : request.fileSize(),
                request == null ? null : request.businessType(),
                objectKey,
                publicUrl,
                "waiting",
                LocalDateTime.now()
        );
        files.put(fileId, draft);
        return new UploadTokenVO(fileId, objectKey, "mock://upload/" + objectKey, publicUrl, EXPIRE_SECONDS);
    }

    @Override
    public StoredFileVO completeUpload(CompleteUploadRequest request) {
        if (request == null || isBlank(request.fileId())) {
            throw new BusinessException(400, "fileId 不能为空");
        }
        StoredFileVO oldFile = files.get(request.fileId());
        if (oldFile == null) {
            throw new BusinessException(404, "文件凭证不存在");
        }

        StoredFileVO uploaded = new StoredFileVO(
                oldFile.fileId(),
                firstNotBlank(request.originalFileName(), oldFile.originalFileName()),
                firstNotBlank(request.contentType(), oldFile.contentType()),
                request.fileSize() == null ? oldFile.fileSize() : request.fileSize(),
                firstNotBlank(request.businessType(), oldFile.businessType()),
                oldFile.objectKey(),
                oldFile.publicUrl(),
                "uploaded",
                oldFile.createdAt()
        );
        files.put(uploaded.fileId(), uploaded);
        return uploaded;
    }

    @Override
    public Optional<StoredFileVO> findByFileId(String fileId) {
        return Optional.ofNullable(files.get(fileId));
    }

    private String buildObjectKey(String fileId, String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            suffix = fileName.substring(dotIndex);
        }
        return datePath + "/" + fileId + suffix;
    }

    private String cleanFileName(String fileName) {
        if (isBlank(fileName)) {
            return "unnamed";
        }
        return fileName.replace("\\", "_").replace("/", "_").trim();
    }

    private String removeLastSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8080/mock-files";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String firstNotBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
