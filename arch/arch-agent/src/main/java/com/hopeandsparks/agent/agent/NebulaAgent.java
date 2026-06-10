package com.hopeandsparks.agent.agent;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.MemoryContext;
import com.hopeandsparks.agent.dto.RetrievalBundle;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.prompt.PromptTemplateService;
import com.hopeandsparks.infra.llm.LlmGateway;
import com.hopeandsparks.infra.llm.LlmRequest;
import com.hopeandsparks.infra.mermaid.MermaidRenderResult;
import com.hopeandsparks.infra.tool.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class NebulaAgent implements SpecialistAgent {

    private final ToolRegistry toolRegistry;
    private final LlmGateway llmGateway;
    private final PromptTemplateService promptTemplateService;

    public NebulaAgent(ToolRegistry toolRegistry, LlmGateway llmGateway, PromptTemplateService promptTemplateService) {
        this.toolRegistry = toolRegistry;
        this.llmGateway = llmGateway;
        this.promptTemplateService = promptTemplateService;
    }

    @Override
    public AgentName name() {
        return AgentName.NEBULA;
    }

    @Override
    public AgentTaskResult execute(AgentRunRequest request, AgentTask task, Map<String, Object> context) {
        MemoryContext memory = context.get("memory") instanceof MemoryContext value ? value : new MemoryContext("", Map.of(), "", Map.of(), List.of());
        RetrievalBundle retrieval = context.get("retrieval") instanceof RetrievalBundle value
                ? value
                : new RetrievalBundle(List.of(), List.of(), List.of(), List.of(), List.of(), false, List.of());
        String description = llmGateway.generate(new LlmRequest(
                promptTemplateService.render("nebula", Map.of(
                        "output_contract", """
                                Return plain text only.
                                First line: diagram type.
                                Then list nodes and edges in concise language.
                                Mermaid constraints:
                                - keep valid flowchart syntax
                                - keep labels short
                                - do not include markdown fences
                                """,
                        "role_boundary", "You define educational flow, graph, and knowledge-map structure. You do not answer the whole question in prose."
                )),
                request.userQuery(),
                List.of(
                        "memory=" + memory.sessionSummary(),
                        "citations=" + retrieval.citations(),
                        "knowledgePoints=" + request.knowledgePointIds()
                ),
                Map.of("agent", "nebula", "mode", request.agentMode(), "taskType", task.taskType().name())
        )).content();
        String script = """
                flowchart TD
                    A[读取问题] --> B[识别知识点]
                    B --> C[拆解关键步骤]
                    C --> D[生成答案]
                    D --> E[复查与输出]
                """;
        String imagePath = "";
        if (request.renderMermaid()) {
            MermaidRenderResult renderResult = (MermaidRenderResult) toolRegistry.call("mermaid_render", Map.of(
                    "diagramScript", script,
                    "outputName", "agent-diagram-" + request.messageId(),
                    "format", "png"
            ));
            imagePath = renderResult.outputPath();
        }
        return new AgentTaskResult(task.taskId(), name(), "COMPLETED",
                description,
                Map.of(
                        "diagramType", "flowchart",
                        "nodeSummary", List.of("读取问题", "识别知识点", "拆解关键步骤", "生成答案", "复查与输出")
                ),
                List.of(),
                false,
                Map.of(
                        "diagramScript", script,
                        "diagramImagePath", imagePath
                ),
                request.renderMermaid() ? List.of("llm_generate", "mermaid_render") : List.of("llm_generate"),
                imagePath.isBlank() && request.renderMermaid() ? List.of("diagram_render_missing") : List.of());
    }
}
