package com.hopeandsparks.kb.vo;

public record KbChunkVO(
        String id,
        String documentId,
        int chunkIndex,
        String contentText,
        String chromaPointId,
        String sectionPath,
        boolean mock
) {
}
