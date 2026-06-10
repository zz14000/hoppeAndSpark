package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.enums.AgentIntent;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.enums.TaskType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TaskPlanner {

    public List<AgentTask> plan(AgentRunRequest request, AgentIntent intent) {
        return switch (intent) {
            case DIAGRAM -> List.of(task("task-diagram", TaskType.MERMAID_DIAGRAM, AgentName.NEBULA, request));
            case STEPS -> List.of(task("task-steps", TaskType.SOLUTION_STEPS, AgentName.COACH, request));
            case RAG -> List.of(task("task-rag", TaskType.KB_RETRIEVAL, AgentName.SAGE, request));
            case PLAN -> List.of(task("task-plan", TaskType.STUDY_PLAN, AgentName.STRICT, request));
            case GRAPH -> List.of(
                    task("task-rag", TaskType.KB_RETRIEVAL, AgentName.SAGE, request),
                    task("task-diagram", TaskType.MERMAID_DIAGRAM, AgentName.NEBULA, request)
            );
            default -> List.of(task("task-qa", TaskType.TEXT_QA, AgentName.SAGE, request));
        };
    }

    private AgentTask task(String id, TaskType type, AgentName agent, AgentRunRequest request) {
        return new AgentTask(id, type, agent, request.userQuery(), 1, List.of(), Map.of(
                "courseName", request.courseName() == null ? "" : request.courseName(),
                "knowledgePoint", request.knowledgePoint() == null ? "" : request.knowledgePoint()
        ));
    }
}
