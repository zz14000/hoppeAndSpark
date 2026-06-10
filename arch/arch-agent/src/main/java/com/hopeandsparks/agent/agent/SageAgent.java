package com.hopeandsparks.agent.agent;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.prompt.PromptTemplateService;
import com.hopeandsparks.infra.llm.LlmGateway;
import com.hopeandsparks.infra.llm.LlmRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SageAgent implements SpecialistAgent {

    private final LlmGateway llmGateway;
    private final PromptTemplateService promptTemplateService;

    public SageAgent(LlmGateway llmGateway, PromptTemplateService promptTemplateService) {
        this.llmGateway = llmGateway;
        this.promptTemplateService = promptTemplateService;
    }

    @Override
    public AgentName name() {
        return AgentName.SAGE;
    }

    @Override
    public AgentTaskResult execute(AgentRunRequest request, AgentTask task, Map<String, Object> context) {
        Map<String, Object> rag = context.get("rag") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
        String content = llmGateway.generate(new LlmRequest(
                promptTemplateService.load("sage").content(),
                request.userQuery(),
                List.of(String.valueOf(rag)),
                Map.of("agent", "sage")
        )).content();
        List<String> citations = citations(rag);
        return new AgentTaskResult(task.taskId(), name(), "COMPLETED", content,
                List.of(), "", citations, false, Map.of("context", "rag"), List.of());
    }

    @SuppressWarnings("unchecked")
    private List<String> citations(Map<String, Object> rag) {
        Object value = rag.get("citations");
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        List<String> fallback = new ArrayList<>();
        Object chunks = rag.get("chunks");
        if (chunks instanceof List<?> list) {
            for (Object item : list) {
                fallback.add(String.valueOf(item));
            }
        }
        return fallback;
    }
}
