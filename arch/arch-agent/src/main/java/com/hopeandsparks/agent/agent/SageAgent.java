package com.hopeandsparks.agent.agent;

import com.hopeandsparks.agent.dto.MemoryContext;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.RetrievalBundle;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.prompt.PromptTemplateService;
import com.hopeandsparks.infra.llm.LlmGateway;
import com.hopeandsparks.infra.llm.LlmRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.LinkedHashMap;
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
        RetrievalBundle retrieval = context.get("retrieval") instanceof RetrievalBundle bundle
                ? bundle
                : new RetrievalBundle(List.of(), List.of(), List.of(), List.of(), List.of(), false, List.of());
        MemoryContext memory = context.get("memory") instanceof MemoryContext memoryContext
                ? memoryContext
                : new MemoryContext("", Map.of(), "", Map.of(), List.of());
        String systemPrompt = promptTemplateService.render("sage", Map.of(
                "output_contract", """
                        Return plain text only.
                        Required structure:
                        1. One concise concept summary.
                        2. One main answer section.
                        3. If retrieval exists, append a short 'Citations:' section using the provided citations only.
                        Forbidden:
                        - Do not fabricate sources.
                        - Do not output Mermaid.
                        - Do not claim web content is authoritative without citations.
                        """,
                "role_boundary", "You explain concepts, answer educational questions, and consume retrieval plus prior specialist context. You do not generate Mermaid diagrams."
        ));
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("agent", "sage");
        metadata.put("mode", request.agentMode());
        metadata.put("taskType", task.taskType().name());
        String content = llmGateway.generate(new LlmRequest(
                systemPrompt,
                request.userQuery(),
                List.of(
                        "memory=" + memory.sessionSummary(),
                        "project=" + memory.projectSummary(),
                        "citations=" + retrieval.citations(),
                        "candidateIds=" + retrieval.candidateIds(),
                        "qualityFlags=" + retrieval.retrievalQualityFlags()
                ),
                metadata
        )).content();
        List<String> citations = retrieval.citations();
        return new AgentTaskResult(task.taskId(), name(), "COMPLETED", content,
                Map.of(
                        "summary", content,
                        "citations", citations,
                        "memorySummary", memory.sessionSummary()
                ),
                citations,
                false,
                Map.of(),
                List.of("llm_generate"),
                citations.isEmpty() && task.requiresRag() ? List.of("missing_citations") : List.of());
    }
}
