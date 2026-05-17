package com.hopeandsparks.domain.vector;


/**
 * 文件职责：VectorSearchCommand 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\vector\VectorSearchCommand.java，用于承载对应分层或接口的基础职责。
 */
import java.util.Map;

public record VectorSearchCommand(
    String collection,
    String query,
    int topK,
    Map<String, Object> filters
) {
}

