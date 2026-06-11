package com.hopeandsparks.agent.agent;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.ResourceBundle;
import com.hopeandsparks.agent.dto.ResourceItem;
import com.hopeandsparks.agent.dto.ResourceSearchRequest;
import com.hopeandsparks.agent.dto.ResourceSearchResult;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.prompt.PromptTemplateService;
import com.hopeandsparks.agent.service.ResourceRetrievalOrchestrator;
import com.hopeandsparks.infra.llm.LlmGateway;
import com.hopeandsparks.infra.llm.LlmRequest;
import com.hopeandsparks.infra.tool.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResourceAgent implements SpecialistAgent {

    private final ResourceRetrievalOrchestrator resourceRetrievalOrchestrator;
    private final ToolRegistry toolRegistry;
    private final LlmGateway llmGateway;
    private final PromptTemplateService promptTemplateService;

    public ResourceAgent(
            ResourceRetrievalOrchestrator resourceRetrievalOrchestrator,
            ToolRegistry toolRegistry,
            LlmGateway llmGateway,
            PromptTemplateService promptTemplateService
    ) {
        this.resourceRetrievalOrchestrator = resourceRetrievalOrchestrator;
        this.toolRegistry = toolRegistry;
        this.llmGateway = llmGateway;
        this.promptTemplateService = promptTemplateService;
    }

    @Override
    public AgentName name() {
        return AgentName.RESOURCE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AgentTaskResult execute(AgentRunRequest request, AgentTask task, Map<String, Object> context) {
        List<AgentTaskResult> priorResults = context.get("priorResults") instanceof List<?> list
                ? (List<AgentTaskResult>) list
                : List.of();
        List<String> preferredTypes = task.rawParams().get("preferredResourceTypes") instanceof List<?> list
                ? list.stream().map(String::valueOf).toList()
                : List.of("article", "video");
        ResourceSearchResult retrieval = resourceRetrievalOrchestrator.retrieve(new ResourceSearchRequest(
                request.userId(),
                request.projectId(),
                request.userQuery(),
                request.knowledgePointIds() == null ? List.of() : request.knowledgePointIds(),
                preferredTypes,
                request.allowWebSearch(),
                Math.max(3, request.maxContextChunks())
        ));
        ResourceBundle bundle = injectDiagram(retrieval.resourceBundle(), priorResults);
        String explanation = llmGateway.generate(new LlmRequest(
                promptTemplateService.render("resource", Map.of(
                        "output_contract", """
                                Return plain text only.
                                Structure:
                                1. One selection summary.
                                2. Video resources.
                                3. Reference resources.
                                4. Practice resources.
                                Do not invent URLs or platforms.
                                """,
                        "role_boundary", "You select and package educational resources, not the main concept answer."
                )),
                request.userQuery(),
                List.of(
                        "preferredTypes=" + preferredTypes,
                        "selectionReason=" + retrieval.selectionDecision().selectionReason(),
                        "qualityFlags=" + retrieval.qualityFlags()
                ),
                Map.of("agent", "resource", "taskType", task.taskType().name())
        )).content();
        toolRegistry.call("resource_bundle_write", Map.of(
                "userId", request.userId(),
                "projectId", request.projectId(),
                "query", request.userQuery(),
                "selectionReason", retrieval.selectionDecision().selectionReason(),
                "videoCount", bundle.videoResources().size(),
                "referenceCount", bundle.referenceResources().size(),
                "practiceCount", bundle.practiceResources().size()
        ));
        Map<String, Object> structuredPayload = new LinkedHashMap<>();
        structuredPayload.put("resourceBundle", bundle);
        structuredPayload.put("resourceDecision", retrieval.selectionDecision());
        structuredPayload.put("selectionReason", retrieval.selectionDecision().selectionReason());
        structuredPayload.put("videoResources", bundle.videoResources());
        structuredPayload.put("referenceResources", bundle.referenceResources());
        structuredPayload.put("practiceResources", bundle.practiceResources());
        structuredPayload.put("qualityFlags", bundle.qualityFlags());
        return new AgentTaskResult(
                task.taskId(),
                name(),
                "COMPLETED",
                explanation,
                "resource.v1",
                Map.copyOf(structuredPayload),
                explanation,
                List.of(),
                false,
                Map.of(
                        "resourceBundle", bundle,
                        "resourceTrace", retrieval.candidateIds(),
                        "resourceDecision", retrieval.selectionDecision()
                ),
                List.of("resource_search", "video_search", "resource_rerank", "resource_bundle_write"),
                bundle.qualityFlags(),
                0.84D,
                explanation
        );
    }

    private ResourceBundle injectDiagram(ResourceBundle bundle, List<AgentTaskResult> priorResults) {
        ResourceItem diagram = priorResults.stream()
                .filter(result -> result.sourceAgent() == AgentName.NEBULA)
                .findFirst()
                .map(result -> new ResourceItem(
                        "diagram-" + result.taskId(),
                        "diagram",
                        "流程图",
                        String.valueOf(result.artifacts().getOrDefault("diagramImagePath", "")),
                        "mermaid",
                        String.valueOf(result.structuredPayload().getOrDefault("diagramIntent", result.renderedText())),
                        "",
                        0L,
                        "",
                        List.of(),
                        result.confidence(),
                        Map.of(
                                "diagramScript", result.artifacts().getOrDefault("diagramScript", ""),
                                "renderHint", result.structuredPayload().getOrDefault("renderHint", "")
                        )
                ))
                .orElse(null);
        return new ResourceBundle(
                diagram,
                bundle.videoResources(),
                bundle.referenceResources(),
                bundle.practiceResources(),
                bundle.qualityFlags(),
                bundle.selectionReason()
        );
    }
}
