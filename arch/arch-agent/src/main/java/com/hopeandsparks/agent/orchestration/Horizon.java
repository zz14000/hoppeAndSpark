package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentExecutionPlan;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.RetrievalBundle;
import com.hopeandsparks.agent.dto.ReviewDecision;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.enums.RevisionTarget;
import com.hopeandsparks.agent.enums.ReviewStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Horizon {

    public ReviewDecision review(
            AgentExecutionPlan plan,
            String answerText,
            List<AgentTaskResult> results,
            RetrievalBundle retrieval,
            Map<String, Object> artifacts
    ) {
        if (answerText == null || answerText.isBlank()) {
            return new ReviewDecision(
                    ReviewStatus.BLOCK,
                    "输出为空",
                    List.of("empty_answer"),
                    List.of("补充可返回内容"),
                    AgentName.SAGE,
                    RevisionTarget.SPECIALIST,
                    false,
                    List.of("answer_empty"),
                    Map.of("reason", "empty_answer")
            );
        }
        if (plan.requiresRag() && retrieval.citations().isEmpty()) {
            return new ReviewDecision(
                    ReviewStatus.REVISE,
                    "RAG回答缺少引用",
                    List.of("missing_citations"),
                    List.of("补充知识库或联网来源引用"),
                    AgentName.SAGE,
                    RevisionTarget.SPECIALIST,
                    true,
                    List.of("citation_missing"),
                    Map.of("reason", "missing_citations")
            );
        }
        if (plan.requiresDiagram() && String.valueOf(artifacts.getOrDefault("diagramScript", "")).isBlank()) {
            return new ReviewDecision(
                    ReviewStatus.REVISE,
                    "图任务缺少图脚本",
                    List.of("missing_diagram"),
                    List.of("生成Mermaid图脚本"),
                    AgentName.NEBULA,
                    RevisionTarget.SPECIALIST,
                    true,
                    List.of("diagram_missing"),
                    Map.of("reason", "missing_diagram")
            );
        }
        boolean webWithoutPermission = retrieval.webSearchUsed() && !Boolean.TRUE.equals(artifacts.getOrDefault("allowWebSearch", Boolean.TRUE));
        if (webWithoutPermission) {
            return new ReviewDecision(
                    ReviewStatus.BLOCK,
                    "触发了未授权的联网检索",
                    List.of("unauthorized_web_search"),
                    List.of("关闭联网或显式授权联网搜索"),
                    null,
                    RevisionTarget.NONE,
                    false,
                    List.of("web_not_allowed"),
                    Map.of("reason", "web_without_permission")
            );
        }
        boolean diagramImageMissing = plan.requiresDiagram()
                && !String.valueOf(artifacts.getOrDefault("diagramScript", "")).isBlank()
                && String.valueOf(artifacts.getOrDefault("diagramImagePath", "")).isBlank();
        return new ReviewDecision(
                ReviewStatus.PUBLISH,
                "review passed",
                List.of(),
                List.of(),
                null,
                RevisionTarget.NONE,
                false,
                diagramImageMissing ? List.of("diagram_failed") : List.of(),
                Map.of("reason", "publish")
        );
    }
}
