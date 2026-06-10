package com.hopeandsparks.kb.vo;

public record KbParseStatusVO(
        String documentId,
        String parseStatus,
        int progress,
        String message,
        boolean mock
) {
}
