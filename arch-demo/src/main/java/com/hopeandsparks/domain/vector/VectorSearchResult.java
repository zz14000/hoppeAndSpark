package com.hopeandsparks.domain.vector;


/**
 * 文件职责：VectorSearchResult 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\vector\VectorSearchResult.java，用于承载对应分层或接口的基础职责。
 */
import java.util.Map;

public record VectorSearchResult(
    String chunkId,
    String content,
    double score,
    Map<String, Object> metadata
) {
}

