package com.hopeandsparks.explore.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * W3 阶段的探索过程记录，后续可映射到 explore_* 过程表。
 */
public class ExploreRecord {

    private String exploreId;
    private Long userId;
    private String query;
    private String domain;
    private String mode;
    private int depth;
    private List<String> goals;
    private List<String> preferredResourceTypes;
    private String summary;
    private List<String> relatedNodes;
    private List<ExploreDraftResource> resources;
    private String taskId;
    private String status;
    private boolean mock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getExploreId() {
        return exploreId;
    }

    public void setExploreId(String exploreId) {
        this.exploreId = exploreId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<String> getGoals() {
        return goals;
    }

    public void setGoals(List<String> goals) {
        this.goals = goals;
    }

    public List<String> getPreferredResourceTypes() {
        return preferredResourceTypes;
    }

    public void setPreferredResourceTypes(List<String> preferredResourceTypes) {
        this.preferredResourceTypes = preferredResourceTypes;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getRelatedNodes() {
        return relatedNodes;
    }

    public void setRelatedNodes(List<String> relatedNodes) {
        this.relatedNodes = relatedNodes;
    }

    public List<ExploreDraftResource> getResources() {
        return resources;
    }

    public void setResources(List<ExploreDraftResource> resources) {
        this.resources = resources;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
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
