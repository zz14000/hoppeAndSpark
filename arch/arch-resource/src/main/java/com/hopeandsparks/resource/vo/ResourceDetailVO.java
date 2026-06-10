package com.hopeandsparks.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Learning resource detail")
public record ResourceDetailVO(
        String id,
        String type,
        String title,
        String summary,
        String knowledgeNodeId,
        String knowledgeKey,
        String knowledgeName,
        String status,
        Integer progress,
        String verifiedBy,
        String detailRoute,
        String detailApi,
        ResourceActionsVO actions,
        ResourceFileVO file,
        List<ResourceVersionVO> versions,
        List<String> tags
) {
}
