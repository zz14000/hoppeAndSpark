package com.hopeandsparks.infrastructure.mock;


/**
 * 文件职责：MockVectorSearchAdapter 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\infrastructure\mock\MockVectorSearchAdapter.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.domain.vector.VectorSearchCommand;
import com.hopeandsparks.domain.vector.VectorSearchGateway;
import com.hopeandsparks.domain.vector.VectorSearchResult;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hope.vector", name = "mode", havingValue = "mock", matchIfMissing = true)
public class MockVectorSearchAdapter implements VectorSearchGateway {

    @Override
    public List<VectorSearchResult> search(VectorSearchCommand command) {
        return List.of(new VectorSearchResult(
            "mock_chunk_1",
            "Mock vector result for: " + command.query(),
            0.99,
            Map.of("collection", command.collection(), "adapter", "mock")
        ));
    }
}

