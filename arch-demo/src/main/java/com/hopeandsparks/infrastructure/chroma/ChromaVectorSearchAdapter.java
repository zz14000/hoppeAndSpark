package com.hopeandsparks.infrastructure.chroma;


/**
 * 文件职责：ChromaVectorSearchAdapter 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\infrastructure\chroma\ChromaVectorSearchAdapter.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.domain.vector.VectorSearchCommand;
import com.hopeandsparks.domain.vector.VectorSearchGateway;
import com.hopeandsparks.domain.vector.VectorSearchResult;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hope.vector", name = "mode", havingValue = "chroma")
public class ChromaVectorSearchAdapter implements VectorSearchGateway {

    @Override
    public List<VectorSearchResult> search(VectorSearchCommand command) {
        throw new UnsupportedOperationException("Chroma adapter is a skeleton. Wire Chroma HTTP client here.");
    }
}

