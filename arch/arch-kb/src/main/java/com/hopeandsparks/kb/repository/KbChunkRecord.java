package com.hopeandsparks.kb.repository;

public record KbChunkRecord(
        Long id,
        Long documentId,
        int chunkIndex,
        String contentText,
        int tokenSize,
        String chromaPointId,
        int embedStatus,
        boolean active,
        String sectionPath
) {
}
