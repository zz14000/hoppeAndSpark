package com.hopeandsparks.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Knowledge point resource network")
public record ResourceNetworkVO(
        String planId,
        String nodeId,
        ResourceNetworkKnowledgeVO knowledge,
        ResourceNetworkStatsVO stats,
        List<ResourceNetworkCategoryVO> categories,
        List<ResourceNetworkResourceNodeVO> resourceNodes,
        List<RelatedKnowledgeNodeVO> relatedKnowledgeNodes,
        List<ResourceNetworkEdgeVO> edges,
        List<String> aiSuggestion
) {
}
