package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.kb.dto.KbDocumentCreateRequest;
import com.hopeandsparks.kb.dto.KbDocumentReparseRequest;
import com.hopeandsparks.kb.dto.KbDocumentUpdateRequest;
import com.hopeandsparks.kb.service.KbDocumentService;
import com.hopeandsparks.kb.vo.KbChunkVO;
import com.hopeandsparks.kb.vo.KbDocumentVO;
import com.hopeandsparks.kb.vo.KbDocumentWriteVO;
import com.hopeandsparks.kb.vo.KbParseStatusVO;
import com.hopeandsparks.manage.service.ManageOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Manage entry for knowledge-base document governance.
 */
@RestController
@RequestMapping("/api/v1/manage/knowledge-base/documents")
public class ManageKnowledgeBaseController {

    private final KbDocumentService kbDocumentService;
    private final ManageOperationLogService manageOperationLogService;

    public ManageKnowledgeBaseController(
            KbDocumentService kbDocumentService,
            ManageOperationLogService manageOperationLogService
    ) {
        this.kbDocumentService = kbDocumentService;
        this.manageOperationLogService = manageOperationLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<KbDocumentVO>> documents(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(kbDocumentService.listDocuments(query));
    }

    @PostMapping
    public ApiResponse<KbDocumentWriteVO> uploadDocument(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @Valid @RequestBody KbDocumentCreateRequest request
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        KbDocumentWriteVO result = kbDocumentService.createDocument(principal, request);
        manageOperationLogService.record(
                principal,
                "kb",
                "create_document",
                "kb_document",
                parseTargetId(result),
                "create kb document: " + request.title(),
                servletRequest
        );
        return ApiResponse.ok("knowledge-base document created", result);
    }

    @PutMapping("/{documentId}")
    public ApiResponse<KbDocumentWriteVO> updateDocument(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String documentId,
            @Valid @RequestBody KbDocumentUpdateRequest request
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        KbDocumentWriteVO result = kbDocumentService.updateDocument(principal, documentId, request);
        manageOperationLogService.record(
                principal,
                "kb",
                Boolean.TRUE.equals(request.reparse()) ? "update_and_reparse_document" : "update_document",
                "kb_document",
                parseTargetId(documentId),
                "update kb document",
                servletRequest
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{documentId}")
    public ApiResponse<KbDocumentWriteVO> deleteDocument(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String documentId
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        KbDocumentWriteVO result = kbDocumentService.deleteDocument(principal, documentId);
        manageOperationLogService.record(
                principal,
                "kb",
                "delete_document",
                "kb_document",
                parseTargetId(documentId),
                "delete kb document",
                servletRequest
        );
        return ApiResponse.ok(result);
    }

    @PostMapping("/{documentId}/reparse")
    public ApiResponse<KbDocumentWriteVO> reparseDocument(
            Authentication authentication,
            HttpServletRequest servletRequest,
            @PathVariable String documentId,
            @RequestBody(required = false) KbDocumentReparseRequest request
    ) {
        AuthenticatedPrincipal principal = principal(authentication);
        KbDocumentWriteVO result = kbDocumentService.reparseDocument(principal, documentId, request);
        manageOperationLogService.record(
                principal,
                "kb",
                "reparse_document",
                "kb_document",
                parseTargetId(documentId),
                request == null ? "manual reparse" : "manual reparse: " + request.reason(),
                servletRequest
        );
        return ApiResponse.ok(result);
    }

    @GetMapping("/{documentId}/parse-status")
    public ApiResponse<KbParseStatusVO> parseStatus(@PathVariable String documentId) {
        return ApiResponse.ok(kbDocumentService.parseStatus(documentId));
    }

    @GetMapping("/{documentId}/chunks")
    public ApiResponse<PageResponse<KbChunkVO>> chunks(
            @PathVariable String documentId,
            @RequestParam Map<String, String> query
    ) {
        return ApiResponse.ok(kbDocumentService.listChunks(documentId, query));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }

    private Long parseTargetId(KbDocumentWriteVO result) {
        if (result == null || result.document() == null) {
            return null;
        }
        return parseTargetId(result.document().id());
    }

    private Long parseTargetId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
