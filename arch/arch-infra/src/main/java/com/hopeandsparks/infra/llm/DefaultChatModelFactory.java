package com.hopeandsparks.infra.llm;

import com.hopeandsparks.infra.config.AiProperties;
import org.springframework.web.reactive.function.client.WebClient;

public class DefaultChatModelFactory implements ChatModelFactory {

    private final AiProperties properties;
    private final WebClient.Builder builder;

    public DefaultChatModelFactory(AiProperties properties, WebClient.Builder builder) {
        this.properties = properties;
        this.builder = builder;
    }

    @Override
    public LlmGateway createGateway() {
        if (realModelConfigured()) {
            return new OpenAiCompatibleLlmGateway(properties, builder);
        }
        return new MockLlmGateway(properties);
    }

    @Override
    public String provider() {
        return properties.getChat().getProvider();
    }

    @Override
    public String modelName() {
        return properties.getChat().getModel();
    }

    @Override
    public boolean realModelConfigured() {
        return properties.getChat().hasApiKey();
    }
}
