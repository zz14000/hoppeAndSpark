package com.hopeandsparks.infrastructure.mock;


/**
 * 文件职责：MockLlmAdapter 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\infrastructure\mock\MockLlmAdapter.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.domain.llm.LlmChatCommand;
import com.hopeandsparks.domain.llm.LlmChatResult;
import com.hopeandsparks.domain.llm.LlmGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hope.llm", name = "mode", havingValue = "mock", matchIfMissing = true)
public class MockLlmAdapter implements LlmGateway {

    @Override
    public LlmChatResult chat(LlmChatCommand command) {
        String provider = command.provider() == null ? "mock" : command.provider();
        return new LlmChatResult(provider, "Mock LLM response for: " + command.prompt());
    }
}

