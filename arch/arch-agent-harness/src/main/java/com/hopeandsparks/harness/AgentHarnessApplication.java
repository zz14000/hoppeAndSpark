package com.hopeandsparks.harness;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.enums.AgentCheckpointPolicy;
import com.hopeandsparks.agent.enums.AgentOutputFormat;
import com.hopeandsparks.agent.enums.AgentRetrievalMode;
import com.hopeandsparks.agent.service.AgentOrchestrationService;
import com.hopeandsparks.agent.vo.AgentRunResultVO;
import com.hopeandsparks.infra.tool.ToolRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(
        scanBasePackages = "com.hopeandsparks",
        exclude = DataSourceAutoConfiguration.class
)
public class AgentHarnessApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentHarnessApplication.class, args);
    }

    @Bean
    CommandLineRunner runHarness(AgentOrchestrationService orchestrationService, ToolRegistry toolRegistry) {
        return args -> {
            Map<String, String> options = parseArgs(args);
            String mode = options.getOrDefault("mode", "qa");
            String query = options.getOrDefault("query", "解释二分查找");
            String userId = options.getOrDefault("userId", "harness-user");
            String projectId = options.getOrDefault("projectId", "default");
            boolean renderMermaid = Boolean.parseBoolean(options.getOrDefault("renderMermaid", "false"));

            AgentRunRequest request = new AgentRunRequest(
                    "harness-request",
                    userId,
                    "harness-session",
                    "harness-message",
                    query,
                    mode,
                    "auto",
                    projectId,
                    options.getOrDefault("courseId", ""),
                    options.getOrDefault("course", ""),
                    options.getOrDefault("knowledgePoint", ""),
                    options.containsKey("knowledgePoint") ? java.util.List.of(options.get("knowledgePoint")) : java.util.List.of(),
                    Boolean.parseBoolean(options.getOrDefault("allowWebSearch", "true")),
                    options.getOrDefault("strictnessLevel", "standard"),
                    renderMermaid,
                    Map.of("harness", true),
                    mode,
                    AgentOutputFormat.STRICT_JSON_WITH_TEXT,
                    Boolean.parseBoolean(options.getOrDefault("allowWebSearch", "true")) ? AgentRetrievalMode.KB_FIRST_CONTROLLED_WEB : AgentRetrievalMode.KB_ONLY,
                    AgentCheckpointPolicy.AUTO,
                    "",
                    "",
                    true,
                    options.getOrDefault("responseStyle", "standard"),
                    Integer.parseInt(options.getOrDefault("maxContextChunks", "5")),
                    Map.of("harness", true)
            );
            AgentRunResultVO result = orchestrationService.run(request);
            System.out.println("=== Agent Harness Result ===");
            System.out.println("mode=" + mode);
            System.out.println("mock=" + result.mock());
            System.out.println("status=" + result.status());
            System.out.println("reviewStatus=" + result.reviewDecision().finalDecision());
            System.out.println();
            System.out.println("=== Answer ===");
            System.out.println(result.finalAnswer());
            if (result.diagramScript() != null && !result.diagramScript().isBlank()) {
                System.out.println("=== Mermaid ===");
                System.out.println(result.diagramScript());
            }
            if (result.diagramImagePath() != null && !result.diagramImagePath().isBlank()) {
                System.out.println("diagramImagePath=" + result.diagramImagePath());
            }
            if (!result.citations().isEmpty()) {
                System.out.println("=== Citations ===");
                result.citations().forEach(System.out::println);
            }
            if (!result.cacheCandidates().isEmpty()) {
                System.out.println("=== Cache Candidates ===");
                result.cacheCandidates().forEach(System.out::println);
            }
            if (!result.taskResults().isEmpty()) {
                System.out.println("=== Task Results ===");
                result.taskResults().forEach(taskResult -> {
                    System.out.println(taskResult.sourceAgent() + " " + taskResult.status() + " " + taskResult.taskId());
                    Object steps = taskResult.structuredPayload().get("steps");
                    if (steps instanceof java.util.List<?> list && !list.isEmpty()) {
                        list.forEach(step -> System.out.println("- " + step));
                    }
                    if (!taskResult.issues().isEmpty()) {
                        taskResult.issues().forEach(error -> System.out.println("issue=" + error));
                    }
                });
            }
            System.out.println("=== Tool Calls ===");
            toolRegistry.recentCalls().forEach(call -> System.out.printf(
                    "%s success=%s durationMs=%d input=%s output=%s failure=%s%n",
                    call.toolName(),
                    call.success(),
                    call.durationMs(),
                    call.inputSummary(),
                    call.outputSummary(),
                    call.failureReason()
            ));
        };
    }

    private Map<String, String> parseArgs(String[] args) {
        Map<String, String> values = new HashMap<>();
        for (String arg : args) {
            if (arg == null || !arg.startsWith("--")) {
                continue;
            }
            String body = arg.substring(2);
            int split = body.indexOf('=');
            if (split > 0) {
                values.put(body.substring(0, split), body.substring(split + 1));
            } else {
                values.put(body, "true");
            }
        }
        return values;
    }
}
