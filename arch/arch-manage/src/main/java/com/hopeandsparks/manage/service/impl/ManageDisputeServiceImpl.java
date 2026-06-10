package com.hopeandsparks.manage.service.impl;

import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.kb.dto.KbChunkCorrectRequest;
import com.hopeandsparks.kb.dto.KbDocumentReparseRequest;
import com.hopeandsparks.kb.service.KbDocumentService;
import com.hopeandsparks.kb.vo.KbChunkCorrectResultVO;
import com.hopeandsparks.kb.vo.KbDocumentWriteVO;
import com.hopeandsparks.manage.dto.DisputeHandleRequest;
import com.hopeandsparks.manage.entity.FeedbackTicket;
import com.hopeandsparks.manage.repository.DisputeRepository;
import com.hopeandsparks.manage.service.ManageDisputeService;
import com.hopeandsparks.manage.vo.ManageDisputeHandleVO;
import com.hopeandsparks.manage.vo.ManageDisputeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Handles dispute tickets and delegates KB corrections to arch-kb.
 */
@Service
public class ManageDisputeServiceImpl implements ManageDisputeService {

    private static final long DEFAULT_PAGE = 1;
    private static final long DEFAULT_PAGE_SIZE = 20;
    private static final long MAX_PAGE_SIZE = 100;

    private final DisputeRepository disputeRepository;
    private final KbDocumentService kbDocumentService;

    public ManageDisputeServiceImpl(DisputeRepository disputeRepository, KbDocumentService kbDocumentService) {
        this.disputeRepository = disputeRepository;
        this.kbDocumentService = kbDocumentService;
    }

    @Override
    public PageResponse<ManageDisputeVO> list(Map<String, String> query) {
        long page = parseLong(value(query, "page"), DEFAULT_PAGE);
        long pageSize = Math.min(parseLong(value(query, "pageSize"), DEFAULT_PAGE_SIZE), MAX_PAGE_SIZE);
        long total = disputeRepository.count(query);
        List<ManageDisputeVO> list = disputeRepository
                .list(query, (page - 1) * pageSize, pageSize)
                .stream()
                .map(this::toVO)
                .toList();
        return PageResponse.of(page, pageSize, total, list);
    }

    @Override
    @Transactional
    public ManageDisputeHandleVO handle(
            AuthenticatedPrincipal principal,
            String disputeId,
            DisputeHandleRequest request
    ) {
        Long adminId = requireAdmin(principal);
        Long parsedDisputeId = requireId(disputeId, "disputeId format is invalid");
        if (request == null) {
            throw new BusinessException(400, "request body cannot be empty");
        }
        FeedbackTicket ticket = disputeRepository.findById(parsedDisputeId)
                .orElseThrow(() -> new BusinessException(404, "dispute ticket does not exist"));

        KbChunkCorrectResultVO chunkCorrection = null;
        if ("chunk".equalsIgnoreCase(ticket.targetType()) && !isBlank(request.correctedContent())) {
            chunkCorrection = kbDocumentService.correctChunk(
                    principal,
                    String.valueOf(ticket.targetId()),
                    new KbChunkCorrectRequest(request.correctedContent(), firstText(request.note(), request.remark()))
            );
        }

        KbDocumentWriteVO reparseTask = null;
        if (!isBlank(request.reparseDocumentId())) {
            reparseTask = kbDocumentService.reparseDocument(
                    principal,
                    request.reparseDocumentId(),
                    new KbDocumentReparseRequest(firstText(request.note(), "dispute handled"))
            );
        }

        String status = normalizeStatus(request.status(), chunkCorrection != null, reparseTask != null);
        String remark = buildRemark(request, chunkCorrection != null, reparseTask != null);
        disputeRepository.updateTicket(ticket.id(), adminId, status, remark);
        FeedbackTicket updated = disputeRepository.findById(ticket.id())
                .orElseThrow(() -> new BusinessException(500, "dispute ticket updated but cannot be read"));
        return new ManageDisputeHandleVO(toVO(updated), chunkCorrection, reparseTask, "dispute handled");
    }

    private String normalizeStatus(String status, boolean corrected, boolean reparsed) {
        if (corrected || reparsed) {
            return "fixed";
        }
        String safeStatus = firstText(status, "reviewed").toLowerCase();
        return switch (safeStatus) {
            case "fixed", "resolved" -> "fixed";
            case "rejected", "reject" -> "rejected";
            case "pending" -> "pending";
            default -> "reviewed";
        };
    }

    private String buildRemark(DisputeHandleRequest request, boolean corrected, boolean reparsed) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, "reviewResult", request.reviewResult());
        appendPart(builder, "result", request.result());
        appendPart(builder, "remark", request.remark());
        appendPart(builder, "note", request.note());
        if (corrected) {
            appendPart(builder, "kbCorrection", "chunk corrected");
        }
        if (reparsed) {
            appendPart(builder, "kbReparse", "document reparse queued");
        }
        String text = builder.toString();
        return text.isBlank() ? "handled by manage" : shortText(text, 500);
    }

    private void appendPart(StringBuilder builder, String key, String value) {
        if (isBlank(value)) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append("; ");
        }
        builder.append(key).append(": ").append(value.trim());
    }

    private ManageDisputeVO toVO(FeedbackTicket ticket) {
        return new ManageDisputeVO(
                String.valueOf(ticket.id()),
                String.valueOf(ticket.userId()),
                ticket.username(),
                ticket.nickname(),
                ticket.targetType(),
                String.valueOf(ticket.targetId()),
                ticket.issueType(),
                ticket.description(),
                ticket.snapshotContent(),
                ticket.status(),
                ticket.adminId() == null ? null : String.valueOf(ticket.adminId()),
                ticket.adminUsername(),
                ticket.processRemark(),
                ticket.createdAt(),
                ticket.updatedAt()
        );
    }

    private Long requireAdmin(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.ADMIN) {
            throw new BusinessException(401, "please login with a manage admin account");
        }
        return principal.id();
    }

    private Long requireId(String value, String message) {
        if (isBlank(value)) {
            throw new BusinessException(400, message);
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException exception) {
            throw new BusinessException(400, message);
        }
    }

    private long parseLong(String value, long defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            return Math.max(Long.parseLong(value.trim()), 1);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private String value(Map<String, String> query, String key) {
        return query == null ? null : query.get(key);
    }

    private String firstText(String first, String second) {
        return isBlank(first) ? second : first.trim();
    }

    private String shortText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
