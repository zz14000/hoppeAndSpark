package com.hopeandsparks.agent.agent;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.enums.AgentName;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StrictAgent implements SpecialistAgent {

    @Override
    public AgentName name() {
        return AgentName.STRICT;
    }

    @Override
    public AgentTaskResult execute(AgentRunRequest request, AgentTask task, Map<String, Object> context) {
        List<String> steps = List.of("第1天: 梳理知识框架", "第2天: 完成例题", "第3天: 复盘错题并调整计划");
        return new AgentTaskResult(task.taskId(), name(), "COMPLETED",
                "Strict mock 学习计划已生成。", steps, "", List.of(), false, Map.of("course", request.courseName()), List.of());
    }
}
