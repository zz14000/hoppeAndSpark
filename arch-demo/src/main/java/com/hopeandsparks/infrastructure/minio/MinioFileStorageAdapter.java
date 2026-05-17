package com.hopeandsparks.infrastructure.minio;


/**
 * 文件职责：MinioFileStorageAdapter 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\infrastructure\minio\MinioFileStorageAdapter.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.domain.file.FileStorageGateway;
import com.hopeandsparks.domain.file.StoreFileCommand;
import com.hopeandsparks.domain.file.StoredFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hope.file", name = "mode", havingValue = "minio")
public class MinioFileStorageAdapter implements FileStorageGateway {

    @Override
    public StoredFile store(StoreFileCommand command) {
        throw new UnsupportedOperationException("MinIO adapter is a skeleton. Wire MinIO client here.");
    }
}

