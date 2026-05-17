package com.hopeandsparks.infrastructure.mock;


/**
 * 文件职责：InMemoryTaskPublisher 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\infrastructure\mock\InMemoryTaskPublisher.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.domain.task.TaskMessage;
import com.hopeandsparks.domain.task.TaskPublisher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hope.queue", name = "mode", havingValue = "mock", matchIfMissing = true)
public class InMemoryTaskPublisher implements TaskPublisher {

    private final List<TaskMessage> messages = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void publish(TaskMessage message) {
        messages.add(message);
    }

    public List<TaskMessage> messages() {
        return List.copyOf(messages);
    }
}

