package com.hopeandsparks.infrastructure.mock;


/**
 * 文件职责：MockFileStorageAdapter 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\infrastructure\mock\MockFileStorageAdapter.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.domain.file.FileStorageGateway;
import com.hopeandsparks.domain.file.StoreFileCommand;
import com.hopeandsparks.domain.file.StoredFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hope.file", name = "mode", havingValue = "mock", matchIfMissing = true)
public class MockFileStorageAdapter implements FileStorageGateway {

    @Override
    public StoredFile store(StoreFileCommand command) {
        long size = command.content() == null ? 0 : command.content().length;
        return new StoredFile(command.bucket(), command.objectKey(), "mock://file/" + command.objectKey(), size);
    }
}

