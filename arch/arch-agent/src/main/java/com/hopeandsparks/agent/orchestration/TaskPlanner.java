package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentExecutionPlan;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.enums.AgentIntent;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.enums.OutputMode;
import com.hopeandsparks.agent.enums.TaskType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TaskPlanner {

    public AgentExecutionPlan plan(AgentRunRequest request, AgentIntent intent) {
        return switch (intent) {
            case DIAGRAM -> plan(
                    "diagram-first explanation",
                    List.of("diagramScript", "finalAnswer"),
                    List.of(
                            task("task-diagram", TaskType.MERMAID_DIAGRAM, AgentName.NEBULA, request, List.of(), true, true, OutputMode.DIAGRAM_FIRST),
                            task("task-qa", TaskType.TEXT_QA, AgentName.SAGE, request, List.of("task-diagram"), true, false, OutputMode.TEXT_WITH_DIAGRAM)
                    ),
                    true,
                    true,
                    OutputMode.DIAGRAM_FIRST
            );
            case STEPS -> plan(
                    "step-by-step coaching",
                    List.of("stepList", "finalAnswer"),
                    List.of(
                            task("task-steps", TaskType.SOLUTION_STEPS, AgentName.COACH, request, List.of(), true, false, OutputMode.TEXT_ONLY),
                            task("task-qa", TaskType.TEXT_QA, AgentName.SAGE, request, List.of("task-steps"), true, false, OutputMode.TEXT_ONLY)
                    ),
                    false,
                    true,
                    OutputMode.TEXT_ONLY
            );
            case RAG -> plan(
                    "retrieval grounded answer",
                    List.of("finalAnswer", "citations"),
                    List.of(task("task-rag", TaskType.KB_RETRIEVAL, AgentName.SAGE, request, List.of(), true, false, OutputMode.TEXT_ONLY)),
                    false,
                    true,
                    OutputMode.TEXT_ONLY
            );
            case PLAN -> plan(
                    "study planning",
                    List.of("learningPlan", "checkpoints"),
                    List.of(task("task-plan", TaskType.STUDY_PLAN, AgentName.STRICT, request, List.of(), true, false, OutputMode.STUDY_PLAN)),
                    false,
                    true,
                    OutputMode.STUDY_PLAN
            );
            case GRAPH -> plan(
                    "graph-first educational explanation",
                    List.of("diagramScript", "finalAnswer", "stepList"),
                    List.of(
                            task("task-diagram", TaskType.MERMAID_DIAGRAM, AgentName.NEBULA, request, List.of(), true, true, OutputMode.DIAGRAM_FIRST),
                            task("task-steps", TaskType.SOLUTION_STEPS, AgentName.COACH, request, List.of("task-diagram"), true, false, OutputMode.TEXT_ONLY),
                            task("task-rag", TaskType.KB_RETRIEVAL, AgentName.SAGE, request, List.of("task-diagram", "task-steps"), true, false, OutputMode.TEXT_WITH_DIAGRAM)
                    ),
                    true,
                    true,
                    OutputMode.DIAGRAM_FIRST
            );
            default -> plan(
                    "plain answer",
                    List.of("finalAnswer"),
                    List.of(task("task-qa", TaskType.TEXT_QA, AgentName.SAGE, request, List.of(), false, false, OutputMode.TEXT_ONLY)),
                    false,
                    false,
                    OutputMode.TEXT_ONLY
            );
        };
    }

    private AgentExecutionPlan plan(
            String primaryGoal,
            List<String> mustProduce,
            List<AgentTask> tasks,
            boolean requiresDiagram,
            boolean requiresRag,
            OutputMode outputMode
    ) {
        List<String> dependencies = tasks.stream().flatMap(task -> task.dependsOn().stream()).distinct().toList();
        return new AgentExecutionPlan(primaryGoal, mustProduce, tasks, dependencies, requiresDiagram, requiresRag, true, outputMode);
    }

    private AgentTask task(
            String id,
            TaskType type,
            AgentName agent,
            AgentRunRequest request,
            List<String> dependsOn,
            boolean requiresRag,
            boolean requiresDiagram,
            OutputMode outputMode
    ) {
        return new AgentTask(id, type, agent, request.userQuery(), 1, dependsOn, requiresRag, requiresDiagram, outputMode, Map.of(
                "courseId", request.courseId() == null ? "" : request.courseId(),
                "courseName", request.courseName() == null ? "" : request.courseName(),
                "knowledgePoint", request.knowledgePoint() == null ? "" : request.knowledgePoint(),
                "knowledgePointIds", request.knowledgePointIds() == null ? List.of() : request.knowledgePointIds(),
                "strictnessLevel", request.strictnessLevel() == null ? "standard" : request.strictnessLevel()
        ));
    }
}
