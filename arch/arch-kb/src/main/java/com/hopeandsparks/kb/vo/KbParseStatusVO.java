package com.hopeandsparks.kb.vo;

import com.hopeandsparks.task.vo.AsyncTaskVO;

/**
 * Polling response for Manage parse status.
 */
public record KbParseStatusVO(
        String documentId,
        String status,
        Integer chunkCount,
        Integer totalTokens,
        Boolean vectorized,
        String errorMessage,
        AsyncTaskVO task
) {
}
