package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentRunRequest;
import com.hopeandsparks.agent.enums.AgentIntent;
import org.springframework.stereotype.Component;

@Component
public class SparkEntry {

    public AgentIntent route(AgentRunRequest request) {
        String mode = request.agentMode() == null ? "" : request.agentMode().toLowerCase();
        String query = request.userQuery() == null ? "" : request.userQuery().toLowerCase();
        if (isGreeting(query)) {
            return AgentIntent.GREETING;
        }
        if ("resource".equals(mode) || query.contains("推荐资料") || query.contains("推荐资源") || query.contains("课程推荐") || query.contains("讲解资源")) {
            return AgentIntent.RESOURCE;
        }
        if ("video".equals(mode) || "video_search".equals(mode) || query.contains("b站") || query.contains("bilibili") || query.contains("视频讲解") || query.contains("lecture") || query.contains("demo")) {
            return AgentIntent.VIDEO_SEARCH;
        }
        if ("training".equals(mode) || query.contains("训练") || query.contains("练习") || query.contains("出题") || query.contains("刷题") || query.contains("批改")) {
            return AgentIntent.TRAINING;
        }
        if ("diagram".equals(mode) || query.contains("流程图") || query.contains("mermaid")) {
            return AgentIntent.DIAGRAM;
        }
        if ("steps".equals(mode) || query.contains("步骤") || query.contains("解题")) {
            return AgentIntent.STEPS;
        }
        if ("rag".equals(mode) || query.contains("知识库") || query.contains("检索")) {
            return AgentIntent.RAG;
        }
        if ("graph".equals(mode)) {
            return AgentIntent.GRAPH;
        }
        if ("plan".equals(mode) || query.contains("学习计划")) {
            return AgentIntent.PLAN;
        }
        return AgentIntent.QA;
    }

    private boolean isGreeting(String query) {
        return query.equals("你好")
                || query.equals("您好")
                || query.equals("hi")
                || query.equals("hello")
                || query.equals("在吗")
                || query.equals("早上好")
                || query.equals("晚上好");
    }
}
