package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentExecutionPlan;
import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.dto.AgentTask;
import com.hopeandsparks.agent.enums.AgentIntent;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.enums.OutputMode;
import com.hopeandsparks.agent.enums.TaskType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskPlanner {

    public AgentExecutionPlan plan(AgentRunRequest request, AgentIntent intent) {
        return switch (intent) {
            case GREETING -> plan(
                    "light greeting response",
                    List.of("finalAnswer"),
                    List.of(task("task-greeting", TaskType.TEXT_QA, AgentName.SAGE, request, List.of(), false, false, false, OutputMode.TEXT_ONLY, List.of(), List.of(), "text_only")),
                    false,
                    false,
                    false,
                    OutputMode.TEXT_ONLY
            );
            case DIAGRAM -> plan(
                    "diagram-first explanation",
                    List.of("diagramScript", "resourceBundle", "finalAnswer"),
                    List.of(
                            task("task-diagram", TaskType.MERMAID_DIAGRAM, AgentName.NEBULA, request, List.of(), true, true, false, OutputMode.DIAGRAM_FIRST, List.of("diagram context"), List.of("diagram"), "resource_bundle"),
                            task("task-resource", TaskType.RESOURCE_RECOMMENDATION, AgentName.RESOURCE, request, List.of("task-diagram"), false, false, true, OutputMode.DIAGRAM_FIRST, List.of("video explainers", "reference links"), List.of("video", "article"), "resource_bundle"),
                            task("task-qa", TaskType.TEXT_QA, AgentName.SAGE, request, List.of("task-diagram", "task-resource"), true, false, false, OutputMode.TEXT_WITH_DIAGRAM, List.of(), List.of(), "text_with_resources")
                    ),
                    true,
                    true,
                    true,
                    OutputMode.DIAGRAM_FIRST
            );
            case STEPS -> plan(
                    "step-by-step coaching",
                    List.of("stepList", "finalAnswer"),
                    List.of(
                            task("task-steps", TaskType.SOLUTION_STEPS, AgentName.COACH, request, List.of(), true, false, false, OutputMode.TEXT_ONLY, List.of(), List.of(), "text_only"),
                            task("task-qa", TaskType.TEXT_QA, AgentName.SAGE, request, List.of("task-steps"), true, false, false, OutputMode.TEXT_ONLY, List.of(), List.of(), "text_only")
                    ),
                    false,
                    false,
                    true,
                    OutputMode.TEXT_ONLY
            );
            case TRAINING -> plan(
                    "training guidance with practice resources",
                    List.of("stepList", "resourceBundle", "finalAnswer"),
                    List.of(
                            task("task-training", TaskType.TRAINING_GUIDE, AgentName.COACH, request, List.of(), true, false, false, OutputMode.TEXT_ONLY, List.of(), List.of(), "text_only"),
                            task("task-resource", TaskType.RESOURCE_RECOMMENDATION, AgentName.RESOURCE, request, List.of("task-training"), false, false, true, OutputMode.TEXT_ONLY, List.of("practice resources", "video explainers"), List.of("practice", "video", "article"), "resource_bundle"),
                            task("task-qa", TaskType.TEXT_QA, AgentName.SAGE, request, List.of("task-training", "task-resource"), true, false, false, OutputMode.TEXT_ONLY, List.of(), List.of(), "text_with_resources")
                    ),
                    false,
                    true,
                    true,
                    OutputMode.TEXT_ONLY
            );
            case RAG -> plan(
                    "retrieval grounded answer",
                    List.of("finalAnswer", "citations"),
                    List.of(task("task-rag", TaskType.KB_RETRIEVAL, AgentName.SAGE, request, List.of(), true, false, false, OutputMode.TEXT_ONLY, List.of(), List.of(), "text_only")),
                    false,
                    false,
                    true,
                    OutputMode.TEXT_ONLY
            );
            case RESOURCE -> plan(
                    "resource-first educational package",
                    List.of("resourceBundle", "finalAnswer"),
                    List.of(
                            task("task-resource", TaskType.RESOURCE_RECOMMENDATION, AgentName.RESOURCE, request, List.of(), false, false, true, OutputMode.TEXT_ONLY, List.of("reference links", "video explainers"), List.of("article", "video"), "resource_bundle"),
                            task("task-qa", TaskType.TEXT_QA, AgentName.SAGE, request, List.of("task-resource"), true, false, false, OutputMode.TEXT_ONLY, List.of(), List.of(), "text_with_resources")
                    ),
                    false,
                    true,
                    true,
                    OutputMode.TEXT_ONLY
            );
            case VIDEO_SEARCH -> plan(
                    "video-first educational package",
                    List.of("resourceBundle", "finalAnswer"),
                    List.of(
                            task("task-video", TaskType.VIDEO_RESOURCE, AgentName.RESOURCE, request, List.of(), false, false, true, OutputMode.TEXT_ONLY, List.of("video explainers"), List.of("video"), "resource_bundle"),
                            task("task-qa", TaskType.TEXT_QA, AgentName.SAGE, request, List.of("task-video"), true, false, false, OutputMode.TEXT_ONLY, List.of(), List.of(), "text_with_resources")
                    ),
                    false,
                    true,
                    true,
                    OutputMode.TEXT_ONLY
            );
            case PLAN -> plan(
                    "study planning with support resources",
                    List.of("learningPlan", "checkpoints", "resourceBundle"),
                    List.of(
                            task("task-plan", TaskType.STUDY_PLAN, AgentName.STRICT, request, List.of(), true, false, false, OutputMode.STUDY_PLAN, List.of(), List.of(), "study_plan"),
                            task("task-resource", TaskType.RESOURCE_RECOMMENDATION, AgentName.RESOURCE, request, List.of("task-plan"), false, false, true, OutputMode.STUDY_PLAN, List.of("follow-up resources"), List.of("article", "video"), "resource_bundle")
                    ),
                    false,
                    true,
                    true,
                    OutputMode.STUDY_PLAN
            );
            case GRAPH -> plan(
                    "graph-first educational explanation with resources",
                    List.of("diagramScript", "stepList", "resourceBundle", "finalAnswer"),
                    List.of(
                            task("task-diagram", TaskType.MERMAID_DIAGRAM, AgentName.NEBULA, request, List.of(), true, true, false, OutputMode.DIAGRAM_FIRST, List.of("diagram context"), List.of("diagram"), "resource_bundle"),
                            task("task-steps", TaskType.SOLUTION_STEPS, AgentName.COACH, request, List.of("task-diagram"), true, false, false, OutputMode.TEXT_ONLY, List.of(), List.of(), "text_only"),
                            task("task-resource", TaskType.RESOURCE_RECOMMENDATION, AgentName.RESOURCE, request, List.of("task-diagram", "task-steps"), false, false, true, OutputMode.TEXT_WITH_DIAGRAM, List.of("video explainers", "reference links"), List.of("video", "article"), "resource_bundle"),
                            task("task-rag", TaskType.KB_RETRIEVAL, AgentName.SAGE, request, List.of("task-diagram", "task-steps", "task-resource"), true, false, false, OutputMode.TEXT_WITH_DIAGRAM, List.of(), List.of(), "text_with_resources")
                    ),
                    true,
                    true,
                    true,
                    OutputMode.DIAGRAM_FIRST
            );
            default -> plan(
                    "plain answer",
                    List.of("finalAnswer"),
                    List.of(task("task-qa", TaskType.TEXT_QA, AgentName.SAGE, request, List.of(), false, false, false, OutputMode.TEXT_ONLY, List.of(), List.of(), "text_only")),
                    false,
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
            boolean requiresResources,
            boolean requiresRag,
            OutputMode outputMode
    ) {
        List<String> dependencies = tasks.stream().flatMap(task -> task.dependsOn().stream()).distinct().toList();
        List<String> resourceGoals = tasks.stream()
                .map(AgentTask::rawParams)
                .map(raw -> raw.get("resourceGoals"))
                .filter(List.class::isInstance)
                .flatMap(value -> ((List<?>) value).stream())
                .map(String::valueOf)
                .distinct()
                .toList();
        List<String> preferredTypes = tasks.stream()
                .map(AgentTask::rawParams)
                .map(raw -> raw.get("preferredResourceTypes"))
                .filter(List.class::isInstance)
                .flatMap(value -> ((List<?>) value).stream())
                .map(String::valueOf)
                .distinct()
                .toList();
        String resourceOutputMode = tasks.stream()
                .map(AgentTask::rawParams)
                .map(raw -> raw.get("resourceOutputMode"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .findFirst()
                .orElse("text_only");
        return new AgentExecutionPlan(
                primaryGoal,
                mustProduce,
                tasks,
                dependencies,
                requiresDiagram,
                requiresResources,
                requiresRag,
                true,
                outputMode,
                resourceGoals,
                preferredTypes,
                resourceOutputMode
        );
    }

    private AgentTask task(
            String id,
            TaskType type,
            AgentName agent,
            AgentRunRequest request,
            List<String> dependsOn,
            boolean requiresRag,
            boolean requiresDiagram,
            boolean requiresResources,
            OutputMode outputMode,
            List<String> resourceGoals,
            List<String> preferredResourceTypes,
            String resourceOutputMode
    ) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("courseId", request.courseId() == null ? "" : request.courseId());
        parameters.put("courseName", request.courseName() == null ? "" : request.courseName());
        parameters.put("knowledgePoint", request.knowledgePoint() == null ? "" : request.knowledgePoint());
        parameters.put("knowledgePointIds", request.knowledgePointIds() == null ? List.of() : request.knowledgePointIds());
        parameters.put("strictnessLevel", request.strictnessLevel() == null ? "standard" : request.strictnessLevel());
        parameters.put("requiresResources", requiresResources);
        parameters.put("resourceGoals", resourceGoals);
        parameters.put("preferredResourceTypes", preferredResourceTypes);
        parameters.put("resourceOutputMode", resourceOutputMode);
        parameters.put("rawQuery", request.userQuery());
        return new AgentTask(id, type, agent, request.userQuery(), 1, dependsOn, requiresRag, requiresDiagram, outputMode, Map.copyOf(parameters));
    }
}
