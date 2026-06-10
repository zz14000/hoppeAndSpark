package com.hopeandsparks.kb.vo;

import com.hopeandsparks.task.vo.AsyncTaskVO;

/**
 * Result returned after a manual chunk correction.
 */
public record KbChunkCorrectResultVO(
        String chunkId,
        String documentId,
        String parseStatus,
        String message,
        AsyncTaskVO task
) {
}
