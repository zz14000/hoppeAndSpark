package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentExecutionPlan;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.RetrievalBundle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Aggregator {

    public String answer(AgentExecutionPlan plan, List<AgentTaskResult> results, RetrievalBundle retrieval) {
        String diagramLead = plan.requiresDiagram() ? "已生成图解，可结合下述说明阅读。\n\n" : "";
        String citations = retrieval.citations().isEmpty() ? "" : "\n\n引用来源:\n" + String.join("\n", retrieval.citations());
        String body = results.stream()
                .map(AgentTaskResult::answerText)
                .filter(text -> text != null && !text.isBlank())
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("暂无可输出内容");
        return diagramLead + body + citations;
    }

    public Map<String, Object> artifacts(AgentExecutionPlan plan, List<AgentTaskResult> results) {
        List<String> steps = results.stream()
                .map(result -> result.artifacts().get("stepList"))
                .filter(List.class::isInstance)
                .flatMap(value -> ((List<?>) value).stream())
                .map(String::valueOf)
                .toList();
        List<String> learningPlan = results.stream()
                .map(result -> result.artifacts().get("learningPlan"))
                .filter(List.class::isInstance)
                .flatMap(value -> ((List<?>) value).stream())
                .map(String::valueOf)
                .toList();
        String diagramScript = results.stream()
                .map(result -> result.artifacts().get("diagramScript"))
                .filter(value -> value != null && !String.valueOf(value).isBlank())
                .map(String::valueOf)
                .findFirst()
                .orElse("");
        String diagramImagePath = results.stream()
                .map(result -> result.artifacts().get("diagramImagePath"))
                .filter(value -> value != null && !String.valueOf(value).isBlank())
                .map(String::valueOf)
                .findFirst()
                .orElse("");
        String finalAnswer = results.stream()
                .map(AgentTaskResult::answerText)
                .filter(value -> value != null && !value.isBlank())
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("");
        List<String> qualityFlags = results.stream()
                .map(AgentTaskResult::issues)
                .flatMap(List::stream)
                .distinct()
                .toList();
        return Map.of(
                "finalAnswer", finalAnswer,
                "stepList", steps,
                "learningPlan", learningPlan,
                "diagramScript", diagramScript,
                "diagramImagePath", diagramImagePath,
                "requiresDiagram", plan.requiresDiagram(),
                "qualityFlags", qualityFlags
        );
    }

    public String diagram(List<AgentTaskResult> results) {
        return results.stream()
                .map(result -> result.artifacts().get("diagramScript"))
                .filter(text -> text != null && !String.valueOf(text).isBlank())
                .map(String::valueOf)
                .findFirst()
                .orElse("");
    }
}
