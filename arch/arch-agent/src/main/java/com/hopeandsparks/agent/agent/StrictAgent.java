package com.hopeandsparks.agent.agent;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.MemoryContext;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.prompt.PromptTemplateService;
import com.hopeandsparks.infra.llm.LlmGateway;
import com.hopeandsparks.infra.llm.LlmRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StrictAgent implements SpecialistAgent {

    private final LlmGateway llmGateway;
    private final PromptTemplateService promptTemplateService;

    public StrictAgent(LlmGateway llmGateway, PromptTemplateService promptTemplateService) {
        this.llmGateway = llmGateway;
        this.promptTemplateService = promptTemplateService;
    }

    @Override
    public AgentName name() {
        return AgentName.STRICT;
    }

    @Override
    public AgentTaskResult execute(AgentRunRequest request, AgentTask task, Map<String, Object> context) {
        MemoryContext memory = context.get("memory") instanceof MemoryContext value ? value : new MemoryContext("", Map.of(), "", Map.of(), List.of());
        String content = llmGateway.generate(new LlmRequest(
                promptTemplateService.render("strict", Map.of(
                        "output_contract", """
                                Return plain text only.
                                Structure:
                                1. Goal summary.
                                2. Plan items.
                                3. Checkpoints.
                                4. Adaptation rules.
                                """,
                        "role_boundary", "You generate structured study plans and checkpoints. You do not create Mermaid diagrams."
                )),
                request.userQuery(),
                List.of(
                        "courseName=" + request.courseName(),
                        "knowledgePoint=" + request.knowledgePoint(),
                        "memory=" + memory.projectSummary()
                ),
                Map.of("agent", "strict", "mode", request.agentMode(), "taskType", task.taskType().name())
        )).content();
        List<String> steps = List.of("第1天: 梳理知识框架", "第2天: 完成例题", "第3天: 复盘错题并调整计划");
        return new AgentTaskResult(task.taskId(), name(), "COMPLETED",
                content,
                Map.of(
                        "planItems", steps,
                        "checkpoints", List.of("完成框架梳理", "完成例题", "完成错题复盘"),
                        "adaptationRules", List.of("若两天内未完成例题则降低计划密度")
                ),
                List.of(),
                false,
                Map.of("learningPlan", steps, "courseName", request.courseName()),
                List.of("llm_generate"),
                List.of());
    }
}
