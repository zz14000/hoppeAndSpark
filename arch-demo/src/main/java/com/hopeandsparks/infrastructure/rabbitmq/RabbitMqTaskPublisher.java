package com.hopeandsparks.infrastructure.rabbitmq;


/**
 * 文件职责：RabbitMqTaskPublisher 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\infrastructure\rabbitmq\RabbitMqTaskPublisher.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.domain.task.TaskMessage;
import com.hopeandsparks.domain.task.TaskPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hope.queue", name = "mode", havingValue = "rabbitmq")
public class RabbitMqTaskPublisher implements TaskPublisher {

    private static final String EXCHANGE = "hope.task.exchange";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqTaskPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(TaskMessage message) {
        rabbitTemplate.convertAndSend(EXCHANGE, message.routingKey(), message);
    }
}

