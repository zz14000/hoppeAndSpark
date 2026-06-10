package com.hopeandsparks.explore.vo;

import com.hopeandsparks.task.vo.AsyncTaskVO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Nebula 探索响应")
public record ExploreVO(
        String exploreId,
        String query,
        String domain,
        String mode,
        String summary,
        List<ExploreResourceVO> resources,
        List<String> relatedNodes,
        AsyncTaskVO task,
        String status,
        boolean mock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
