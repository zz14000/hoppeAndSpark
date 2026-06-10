package com.hopeandsparks.agent.agent;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.infra.mermaid.MermaidRenderResult;
import com.hopeandsparks.infra.tool.ToolRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class NebulaAgent implements SpecialistAgent {

    private final ToolRegistry toolRegistry;

    public NebulaAgent(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @Override
    public AgentName name() {
        return AgentName.NEBULA;
    }

    @Override
    public AgentTaskResult execute(AgentRunRequest request, AgentTask task, Map<String, Object> context) {
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
                "Nebula mock Mermaid 图已生成。", List.of(), script, List.of(), false,
                Map.of("diagramImagePath", imagePath), List.of());
    }
}
