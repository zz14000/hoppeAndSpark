package com.hopeandsparks.domain.file;


/**
 * 文件职责：StoreFileCommand 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\file\StoreFileCommand.java，用于承载对应分层或接口的基础职责。
 */
public record StoreFileCommand(
    String bucket,
    String objectKey,
    String contentType,
    byte[] content
) {
}

