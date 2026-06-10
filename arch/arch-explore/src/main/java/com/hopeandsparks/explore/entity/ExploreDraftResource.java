package com.hopeandsparks.explore.entity;

/**
 * Nebula 探索阶段的资源草案，不代表 learning_resource 已落库。
 */
public record ExploreDraftResource(
        String id,
        String type,
        String title,
        String summary,
        String nodeId,
        String status
) {
}
