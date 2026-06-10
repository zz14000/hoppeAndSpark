package com.hopeandsparks.agent.agent;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.enums.AgentName;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CoachAgent implements SpecialistAgent {

    @Override
    public AgentName name() {
        return AgentName.COACH;
    }

    @Override
    public AgentTaskResult execute(AgentRunRequest request, AgentTask task, Map<String, Object> context) {
        List<String> steps = List.of(
                "读题并标出已知条件",
                "定位相关知识点: " + safe(request.knowledgePoint(), "待识别"),
                "列出关键公式或推理关系",
                "按步骤求解并检查边界条件"
        );
        return new AgentTaskResult(task.taskId(), name(), "COMPLETED",
                "Coach mock 解题步骤已生成。", steps, "", List.of(), false, Map.of(), List.of());
    }

    private String safe(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
