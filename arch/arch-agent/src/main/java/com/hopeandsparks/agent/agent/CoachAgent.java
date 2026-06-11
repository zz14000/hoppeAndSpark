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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CoachAgent implements SpecialistAgent {

    private final LlmGateway llmGateway;
    private final PromptTemplateService promptTemplateService;

    public CoachAgent(LlmGateway llmGateway, PromptTemplateService promptTemplateService) {
        this.llmGateway = llmGateway;
        this.promptTemplateService = promptTemplateService;
    }

    @Override
    public AgentName name() {
        return AgentName.COACH;
    }

    @Override
    public AgentTaskResult execute(AgentRunRequest request, AgentTask task, Map<String, Object> context) {
        MemoryContext memory = context.get("memory") instanceof MemoryContext value ? value : new MemoryContext("", Map.of(), "", Map.of(), List.of());
        RetrievalBundle retrieval = context.get("retrieval") instanceof RetrievalBundle value
                ? value
                : new RetrievalBundle(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null, List.of(), List.of(), false, List.of(), Map.of());
        String content = llmGateway.generate(new LlmRequest(
                promptTemplateService.render("coach", Map.of(
                        "output_contract", """
                                Return plain text only.
                                Structure:
                                1. Short diagnosis line.
                                2. Numbered steps.
                                3. Hints.
                                4. Common mistakes.
                                Do not output Mermaid or learning plans.
                                """,
                        "role_boundary", "You produce step-by-step educational guidance, not final diagram generation."
                )),
                request.userQuery(),
                List.of(
                        "knowledgePoint=" + safe(request.knowledgePoint(), "待识别"),
                        "memory=" + memory.sessionSummary(),
                        "citations=" + retrieval.citations()
                ),
                Map.of("agent", "coach", "mode", request.agentMode(), "taskType", task.taskType().name())
        )).content();
        List<String> steps = List.of(
                "读题并标出已知条件",
                "定位相关知识点: " + safe(request.knowledgePoint(), "待识别"),
                "列出关键公式或推理关系",
                "按步骤求解并检查边界条件"
        );
        return new AgentTaskResult(task.taskId(), name(), "COMPLETED",
                content,
                "coach.v1",
                Map.of(
                        "steps", steps,
                        "hints", List.of("先看题目条件", "再看边界条件"),
                        "commonMistakes", List.of("忽略边界", "没有验证结果")
                ),
                content,
                List.of(),
                false,
                Map.of("stepList", steps),
                List.of("llm_generate"),
                List.of(),
                0.88D,
                content);
    }

    private String safe(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
