package com.hopeandsparks.kb.vo;

import com.hopeandsparks.task.vo.AsyncTaskVO;

/**
 * Response for create, update, delete, and reparse actions.
 */
public record KbDocumentWriteVO(
        KbDocumentVO document,
        AsyncTaskVO task,
        String message
) {
}
