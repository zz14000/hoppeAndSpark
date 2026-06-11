package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentExecutionPlan;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.CitationVO;
import com.hopeandsparks.agent.dto.DiagramArtifactVO;
import com.hopeandsparks.agent.dto.FinalAnswerEnvelope;
import com.hopeandsparks.agent.dto.ResourceBundle;
import com.hopeandsparks.agent.dto.ResourceItem;
import com.hopeandsparks.agent.dto.RetrievalBundle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Aggregator {

    public String answer(AgentExecutionPlan plan, List<AgentTaskResult> results, RetrievalBundle retrieval) {
        Map<String, Object> artifacts = artifacts(plan, results);
        String conclusion = firstText(results, "暂无可输出内容");
        String details = results.stream()
                .map(AgentTaskResult::answerText)
                .filter(text -> text != null && !text.isBlank())
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("暂无可输出内容");
        String steps = castList(artifacts.get("stepList")).isEmpty() ? "" : "\n\n步骤:\n- " + String.join("\n- ", castList(artifacts.get("stepList")));
        String diagram = String.valueOf(artifacts.getOrDefault("diagramScript", "")).isBlank() ? "" : "\n\n图解说明:\n已生成图解资源，可结合图脚本或渲染图查看。";
        String resources = resourceSummary(resourceBundle(results));
        String citations = retrieval == null || retrieval.citations().isEmpty() ? "" : "\n\n引用来源:\n" + String.join("\n", retrieval.citations());
        return "结论:\n" + conclusion + "\n\n详细说明:\n" + details + steps + diagram + resources + citations;
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
        ResourceBundle resourceBundle = resourceBundle(results);
        List<String> resourceTrace = results.stream()
                .map(result -> result.artifacts().get("resourceTrace"))
                .filter(List.class::isInstance)
                .flatMap(value -> ((List<?>) value).stream())
                .map(String::valueOf)
                .distinct()
                .toList();
        return Map.of(
                "finalAnswer", finalAnswer,
                "stepList", steps,
                "learningPlan", learningPlan,
                "diagramScript", diagramScript,
                "diagramImagePath", diagramImagePath,
                "resourceBundle", resourceBundle,
                "resourceTrace", resourceTrace,
                "requiresDiagram", plan.requiresDiagram(),
                "requiresResources", plan.requiresResources(),
                "qualityFlags", qualityFlags
        );
    }

    public FinalAnswerEnvelope envelope(AgentExecutionPlan plan, List<AgentTaskResult> results, RetrievalBundle retrieval, Map<String, Object> artifacts) {
        String finalAnswer = String.valueOf(artifacts.getOrDefault("finalAnswer", ""));
        String summary = results.stream()
                .map(AgentTaskResult::renderedText)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .map(text -> text.length() > 160 ? text.substring(0, 160) + "..." : text)
                .orElse(finalAnswer);
        DiagramArtifactVO diagram = new DiagramArtifactVO(
                "diagram",
                String.valueOf(artifacts.getOrDefault("diagramScript", "")),
                String.valueOf(artifacts.getOrDefault("diagramImagePath", "")),
                results.stream().filter(result -> result.sourceAgent().name().equals("NEBULA")).map(AgentTaskResult::renderedText).findFirst().orElse("")
        );
        List<CitationVO> citations = retrieval == null ? List.of() : retrieval.fusedHits().stream()
                .limit(5)
                .map(hit -> new CitationVO(hit.title(), hit.url(), hit.sourceType(), hit.score()))
                .toList();
        ResourceBundle resourceBundle = artifacts.get("resourceBundle") instanceof ResourceBundle bundle ? bundle : emptyResourceBundle();
        return new FinalAnswerEnvelope(
                summary,
                summary,
                finalAnswer,
                castList(artifacts.get("stepList")),
                diagram,
                citations,
                castList(artifacts.get("learningPlan")),
                resourceBundle,
                summarizeResources(resourceBundle),
                castList(artifacts.get("qualityFlags")),
                Map.of(
                        "requiresDiagram", plan.requiresDiagram(),
                        "requiresRag", plan.requiresRag(),
                        "requiresResources", plan.requiresResources()
                )
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

    @SuppressWarnings("unchecked")
    private List<String> castList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private ResourceBundle resourceBundle(List<AgentTaskResult> results) {
        return results.stream()
                .map(AgentTaskResult::artifacts)
                .map(artifacts -> artifacts.get("resourceBundle"))
                .filter(ResourceBundle.class::isInstance)
                .map(ResourceBundle.class::cast)
                .findFirst()
                .orElse(emptyResourceBundle());
    }

    private ResourceBundle emptyResourceBundle() {
        return new ResourceBundle(null, List.of(), List.of(), List.of(), List.of(), "");
    }

    private String firstText(List<AgentTaskResult> results, String defaultValue) {
        return results.stream()
                .map(AgentTaskResult::answerText)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .orElse(defaultValue);
    }

    private String resourceSummary(ResourceBundle bundle) {
        String summary = summarizeResources(bundle);
        return summary.isBlank() ? "" : "\n\n推荐资源:\n" + summary;
    }

    private String summarizeResources(ResourceBundle bundle) {
        if (bundle == null) {
            return "";
        }
        List<String> lines = new java.util.ArrayList<>();
        if (!bundle.videoResources().isEmpty()) {
            lines.add("视频: " + joinTitles(bundle.videoResources()));
        }
        if (!bundle.referenceResources().isEmpty()) {
            lines.add("资料: " + joinTitles(bundle.referenceResources()));
        }
        if (!bundle.practiceResources().isEmpty()) {
            lines.add("练习: " + joinTitles(bundle.practiceResources()));
        }
        return String.join("\n", lines);
    }

    private String joinTitles(List<ResourceItem> items) {
        return items.stream().limit(3).map(ResourceItem::title).reduce((left, right) -> left + " / " + right).orElse("");
    }
}
