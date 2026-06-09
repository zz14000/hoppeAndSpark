package com.hopeandsparks.infra.file;

import java.util.Optional;

/**
 * 文件存储服务接口。
 * W1 先用 mock 实现，业务模块只拿 fileId 和访问地址，不直接接触 MinIO。
 */
public interface FileStorageService {

    UploadTokenVO createUploadToken(UploadTokenRequest request);

    StoredFileVO completeUpload(CompleteUploadRequest request);

    Optional<StoredFileVO> findByFileId(String fileId);
}
