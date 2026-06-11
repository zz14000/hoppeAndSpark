package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentExecutionPlan;
import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.ResourceBundle;
import com.hopeandsparks.agent.dto.ResourceItem;
import com.hopeandsparks.agent.dto.ReviewDecision;
import com.hopeandsparks.agent.dto.ReviewIssueVO;
import com.hopeandsparks.agent.dto.RetrievalBundle;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.enums.AgentNodeStatus;
import com.hopeandsparks.agent.enums.RevisionTarget;
import com.hopeandsparks.agent.enums.ReviewStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
            return decision(
                    ReviewStatus.BLOCK,
                    "输出为空",
                    List.of("empty_answer"),
                    List.of("补充可返回内容"),
                    AgentName.SAGE,
                    RevisionTarget.SPECIALIST,
                    false,
                    List.of("answer_empty"),
                    List.of(new ReviewIssueVO("empty_answer", "输出为空", "HIGH")),
                    List.of(),
                    RevisionTarget.NONE,
                    AgentNodeStatus.FAILED,
                    Map.of("reason", "empty_answer")
            );
        }
        if (plan.requiresRag() && retrieval.citations().isEmpty()) {
            return decision(
                    ReviewStatus.REVISE,
                    "RAG回答缺少引用",
                    List.of("missing_citations"),
                    List.of("补充知识库或联网来源引用"),
                    AgentName.SAGE,
                    RevisionTarget.SPECIALIST,
                    true,
                    List.of("citation_missing"),
                    List.of(new ReviewIssueVO("missing_citations", "RAG回答缺少引用", "MEDIUM")),
                    List.of(),
                    RevisionTarget.NONE,
                    AgentNodeStatus.FAILED,
                    Map.of("reason", "missing_citations")
            );
        }
        if (plan.requiresDiagram() && String.valueOf(artifacts.getOrDefault("diagramScript", "")).isBlank()) {
            return decision(
                    ReviewStatus.REVISE,
                    "图任务缺少图脚本",
                    List.of("missing_diagram"),
                    List.of("生成Mermaid图脚本"),
                    AgentName.NEBULA,
                    RevisionTarget.SPECIALIST,
                    true,
                    List.of("diagram_missing"),
                    List.of(new ReviewIssueVO("missing_diagram", "图任务缺少图脚本", "MEDIUM")),
                    List.of(),
                    RevisionTarget.NONE,
                    AgentNodeStatus.FAILED,
                    Map.of("reason", "missing_diagram")
            );
        }
        ResourceBundle bundle = artifacts.get("resourceBundle") instanceof ResourceBundle resourceBundle
                ? resourceBundle
                : new ResourceBundle(null, List.of(), List.of(), List.of(), List.of(), "");
        if (plan.requiresResources() && isEmptyBundle(bundle)) {
            return decision(
                    ReviewStatus.REVISE,
                    "资源任务缺少资源包",
                    List.of("missing_resources"),
                    List.of("补充视频、资料或练习资源"),
                    AgentName.RESOURCE,
                    RevisionTarget.SPECIALIST,
                    true,
                    List.of("resource_missing"),
                    List.of(new ReviewIssueVO("missing_resources", "资源任务缺少资源包", "MEDIUM")),
                    List.of("missing_resources"),
                    RevisionTarget.SPECIALIST,
                    AgentNodeStatus.FAILED,
                    Map.of("reason", "missing_resources")
            );
        }
        if (plan.preferredResourceTypes().contains("video") && bundle.videoResources().isEmpty()) {
            return decision(
                    ReviewStatus.REVISE,
                    "需要视频资源但未命中视频结果",
                    List.of("missing_video_resources"),
                    List.of("补充 B 站或其他允许平台的视频讲解资源"),
                    AgentName.RESOURCE,
                    RevisionTarget.SPECIALIST,
                    true,
                    List.of("video_missing"),
                    List.of(new ReviewIssueVO("missing_video_resources", "需要视频资源但未命中视频结果", "MEDIUM")),
                    List.of("missing_video_resources"),
                    RevisionTarget.SPECIALIST,
                    AgentNodeStatus.FAILED,
                    Map.of("reason", "missing_video_resources")
            );
        }
        List<String> invalidResources = invalidResources(bundle);
        if (!invalidResources.isEmpty()) {
            return decision(
                    ReviewStatus.REVISE,
                    "外部资源缺少必要元数据",
                    List.of("invalid_resource_metadata"),
                    List.of("补齐标题、平台来源与摘要"),
                    AgentName.RESOURCE,
                    RevisionTarget.SPECIALIST,
                    true,
                    List.of("resource_metadata_invalid"),
                    List.of(new ReviewIssueVO("invalid_resource_metadata", "外部资源缺少必要元数据", "MEDIUM")),
                    invalidResources,
                    RevisionTarget.SPECIALIST,
                    AgentNodeStatus.FAILED,
                    Map.of("reason", "invalid_resource_metadata")
            );
        }
        boolean webWithoutPermission = retrieval.webSearchUsed() && !Boolean.TRUE.equals(artifacts.getOrDefault("allowWebSearch", Boolean.TRUE));
        if (webWithoutPermission) {
            return decision(
                    ReviewStatus.BLOCK,
                    "触发了未授权的联网检索",
                    List.of("unauthorized_web_search"),
                    List.of("关闭联网或显式授权联网搜索"),
                    null,
                    RevisionTarget.NONE,
                    false,
                    List.of("web_not_allowed"),
                    List.of(new ReviewIssueVO("unauthorized_web_search", "触发了未授权的联网检索", "HIGH")),
                    List.of("unauthorized_web_search"),
                    RevisionTarget.NONE,
                    AgentNodeStatus.FAILED,
                    Map.of("reason", "web_without_permission")
            );
        }
        boolean diagramImageMissing = plan.requiresDiagram()
                && !String.valueOf(artifacts.getOrDefault("diagramScript", "")).isBlank()
                && String.valueOf(artifacts.getOrDefault("diagramImagePath", "")).isBlank();
        List<String> qualityFlags = new ArrayList<>();
        if (diagramImageMissing) {
            qualityFlags.add("diagram_failed");
        }
        qualityFlags.addAll(bundle.qualityFlags());
        return decision(
                ReviewStatus.PUBLISH,
                "review passed",
                List.of(),
                List.of(),
                null,
                RevisionTarget.NONE,
                false,
                qualityFlags,
                diagramImageMissing ? List.of(new ReviewIssueVO("diagram_failed", "Mermaid 图片生成失败，文本已降级发布", "LOW")) : List.of(),
                List.of(),
                RevisionTarget.NONE,
                AgentNodeStatus.SUCCESS,
                Map.of("reason", "publish")
        );
    }

    private ReviewDecision decision(
            ReviewStatus status,
            String summary,
            List<String> issues,
            List<String> fixSuggestions,
            AgentName targetAgent,
            RevisionTarget revisionTarget,
            boolean repairable,
            List<String> qualityFlags,
            List<ReviewIssueVO> reviewIssues,
            List<String> resourceIssues,
            RevisionTarget resourceRevisionTarget,
            AgentNodeStatus nodeStatus,
            Map<String, Object> metadata
    ) {
        return new ReviewDecision(
                status,
                summary,
                issues,
                fixSuggestions,
                targetAgent,
                revisionTarget,
                repairable,
                qualityFlags,
                reviewIssues,
                resourceIssues,
                resourceRevisionTarget,
                nodeStatus,
                metadata
        );
    }

    private boolean isEmptyBundle(ResourceBundle bundle) {
        return bundle == null
                || (bundle.primaryDiagram() == null
                && bundle.videoResources().isEmpty()
                && bundle.referenceResources().isEmpty()
                && bundle.practiceResources().isEmpty());
    }

    private List<String> invalidResources(ResourceBundle bundle) {
        List<String> invalid = new ArrayList<>();
        if (bundle == null) {
            return invalid;
        }
        List<ResourceItem> items = new ArrayList<>();
        items.addAll(bundle.videoResources());
        items.addAll(bundle.referenceResources());
        items.addAll(bundle.practiceResources());
        for (ResourceItem item : items) {
            if (item.title() == null || item.title().isBlank() || item.source() == null || item.source().isBlank() || item.summary() == null || item.summary().isBlank()) {
                invalid.add(item.resourceId());
            }
        }
        return invalid;
    }
}
