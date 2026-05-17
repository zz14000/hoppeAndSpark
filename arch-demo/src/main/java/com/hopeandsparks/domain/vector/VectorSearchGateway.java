package com.hopeandsparks.domain.vector;


/**
 * 文件职责：VectorSearchGateway 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\domain\vector\VectorSearchGateway.java，用于承载对应分层或接口的基础职责。
 */
import java.util.List;

public interface VectorSearchGateway {

    List<VectorSearchResult> search(VectorSearchCommand command);
}

