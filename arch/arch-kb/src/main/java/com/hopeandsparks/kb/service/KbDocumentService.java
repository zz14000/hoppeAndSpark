package com.hopeandsparks.kb.service;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.kb.dto.KbChunkCorrectRequest;
import com.hopeandsparks.kb.dto.KbDocumentCreateRequest;
import com.hopeandsparks.kb.dto.KbDocumentReparseRequest;
import com.hopeandsparks.kb.dto.KbDocumentUpdateRequest;
import com.hopeandsparks.kb.vo.KbChunkCorrectResultVO;
import com.hopeandsparks.kb.vo.KbChunkVO;
import com.hopeandsparks.kb.vo.KbDocumentVO;
import com.hopeandsparks.kb.vo.KbDocumentWriteVO;
import com.hopeandsparks.kb.vo.KbParseStatusVO;

import java.util.Map;

/**
 * Knowledge-base document business boundary.
 *
 * <p>Manage controllers call this service for document writes and parse state
 * changes. The KB module owns {@code kb_document} and {@code kb_chunk_record};
 * file storage, Redis Stream, and async task details stay behind infra/task
 * services.</p>
 */
public interface KbDocumentService {

    PageResponse<KbDocumentVO> listDocuments(Map<String, String> query);

    KbDocumentWriteVO createDocument(AuthenticatedPrincipal principal, KbDocumentCreateRequest request);

    KbDocumentWriteVO updateDocument(AuthenticatedPrincipal principal, String documentId, KbDocumentUpdateRequest request);

    KbDocumentWriteVO deleteDocument(AuthenticatedPrincipal principal, String documentId);

    KbDocumentWriteVO reparseDocument(AuthenticatedPrincipal principal, String documentId, KbDocumentReparseRequest request);

    KbParseStatusVO parseStatus(String documentId);

    PageResponse<KbChunkVO> listChunks(String documentId, Map<String, String> query);

    KbChunkCorrectResultVO correctChunk(
            AuthenticatedPrincipal principal,
            String chunkId,
            KbChunkCorrectRequest request
    );

    int consumePendingParseMessages();

    void parseDocument(String documentId, String taskId);
}
