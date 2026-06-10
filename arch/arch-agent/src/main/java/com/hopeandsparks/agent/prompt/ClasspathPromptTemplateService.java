package com.hopeandsparks.agent.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ClasspathPromptTemplateService implements PromptTemplateService {

    @Override
    public PromptTemplate load(String agentName) {
        String normalized = agentName == null || agentName.isBlank() ? "sage" : agentName.toLowerCase();
        ClassPathResource resource = new ClassPathResource("prompts/" + normalized + ".md");
        if (!resource.exists()) {
            return new PromptTemplate(normalized, "Prompt not found: " + normalized, true);
        }
        try {
            return new PromptTemplate(normalized, resource.getContentAsString(StandardCharsets.UTF_8), false);
        } catch (IOException exception) {
            return new PromptTemplate(normalized, "Prompt load failed: " + exception.getMessage(), true);
        }
    }

    @Override
    public String render(String agentName, java.util.Map<String, Object> variables) {
        String content = load(agentName).content();
        String rendered = content;
        if (variables == null || variables.isEmpty()) {
            return rendered;
        }
        for (var entry : variables.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return rendered;
    }
}
