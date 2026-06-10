package com.hopeandsparks.manage.vo;

import com.hopeandsparks.kb.vo.KbChunkCorrectResultVO;
import com.hopeandsparks.kb.vo.KbDocumentWriteVO;

/**
 * Result of handling a dispute ticket.
 */
public record ManageDisputeHandleVO(
        ManageDisputeVO dispute,
        KbChunkCorrectResultVO chunkCorrection,
        KbDocumentWriteVO reparseTask,
        String message
) {
}
