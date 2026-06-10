package com.hopeandsparks.explore.vo;

import com.hopeandsparks.task.vo.AsyncTaskVO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "思维导图响应")
public record MindMapVO(
        String mindmapId,
        String exploreId,
        String style,
        List<MindMapNodeVO> nodes,
        List<MindMapEdgeVO> edges,
        AsyncTaskVO task
) {
}
