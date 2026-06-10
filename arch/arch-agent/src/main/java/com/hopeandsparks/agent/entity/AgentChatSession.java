package com.hopeandsparks.agent.entity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * W3 阶段的本地会话记录，对应 agent_chat_session 的核心字段。
 */
public class AgentChatSession {

    private String sessionId;
    private Long userId;
    private String agentId;
    private String title;
    private String source;
    private String contextNodeId;
    private String contextResourceId;
    private Map<String, Object> context;
    private Map<String, Object> metadata;
    private String status;
    private String externalConversationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getContextNodeId() {
        return contextNodeId;
    }

    public void setContextNodeId(String contextNodeId) {
        this.contextNodeId = contextNodeId;
    }

    public String getContextResourceId() {
        return contextResourceId;
    }

    public void setContextResourceId(String contextResourceId) {
        this.contextResourceId = contextResourceId;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExternalConversationId() {
        return externalConversationId;
    }

    public void setExternalConversationId(String externalConversationId) {
        this.externalConversationId = externalConversationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
