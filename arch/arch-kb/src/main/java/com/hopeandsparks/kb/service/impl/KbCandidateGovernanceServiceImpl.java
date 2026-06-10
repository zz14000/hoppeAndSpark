package com.hopeandsparks.kb.service.impl;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.chroma.ChromaVectorStoreGateway;
import com.hopeandsparks.infra.chroma.RetrievedChunk;
import com.hopeandsparks.infra.chroma.VectorSearchRequest;
import com.hopeandsparks.infra.chroma.VectorSearchResponse;
import com.hopeandsparks.infra.config.KbProperties;
import com.hopeandsparks.infra.embedding.EmbeddingGateway;
import com.hopeandsparks.infra.embedding.EmbeddingRequest;
import com.hopeandsparks.infra.rerank.RerankGateway;
import com.hopeandsparks.infra.rerank.RerankRequest;
import com.hopeandsparks.infra.rerank.RerankResponse;
import com.hopeandsparks.infra.rerank.RerankResult;
import com.hopeandsparks.infra.search.WebSearchResult;
import com.hopeandsparks.kb.dto.KbDocumentCreateRequest;
import com.hopeandsparks.kb.repository.KbCandidateGovernanceRepository;
import com.hopeandsparks.kb.repository.KbCandidateRecord;
import com.hopeandsparks.kb.repository.KbDocumentRecord;
import com.hopeandsparks.kb.repository.KbDocumentRepository;
import com.hopeandsparks.kb.service.GovernanceResult;
import com.hopeandsparks.kb.service.KbCandidateGovernanceService;
import com.hopeandsparks.kb.service.KbDocumentService;
import com.hopeandsparks.kb.vo.KbDocumentWriteVO;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class KbCandidateGovernanceServiceImpl implements KbCandidateGovernanceService {

    private static final String CANDIDATE_COLLECTION = "web_cache_candidates";
    private static final String FORMAL_COLLECTION = "edu_ground_truth";

    private final KbCandidateGovernanceRepository repository;
    private final KbDocumentRepository kbDocumentRepository;
    private final KbDocumentService kbDocumentService;
    private final ChromaVectorStoreGateway chromaVectorStoreGateway;
    private final EmbeddingGateway embeddingGateway;
    private final RerankGateway rerankGateway;
    private final KbProperties kbProperties;

    public KbCandidateGovernanceServiceImpl(
            KbCandidateGovernanceRepository repository,
            KbDocumentRepository kbDocumentRepository,
            KbDocumentService kbDocumentService,
            ChromaVectorStoreGateway chromaVectorStoreGateway,
            EmbeddingGateway embeddingGateway,
            RerankGateway rerankGateway,
            KbProperties kbProperties
    ) {
        this.repository = repository;
        this.kbDocumentRepository = kbDocumentRepository;
        this.kbDocumentService = kbDocumentService;
        this.chromaVectorStoreGateway = chromaVectorStoreGateway;
        this.embeddingGateway = embeddingGateway;
        this.rerankGateway = rerankGateway;
        this.kbProperties = kbProperties;
    }

    @Override
    public GovernanceResult governDocument(String documentId, String userId, String projectId, String collection) {
        if (blank(collection) || FORMAL_COLLECTION.equals(collection)) {
            return new GovernanceResult("SKIPPED", "", "formal collection skip governance", false);
        }
        KbDocumentRecord document = kbDocumentRepository.findDocument(parseLong(documentId))
                .orElseThrow(() -> new IllegalArgumentException("KB document not found: " + documentId));
        if (!CANDIDATE_COLLECTION.equals(collection) && !"url".equalsIgnoreCase(document.sourceType())) {
            return new GovernanceResult("SKIPPED", "", "non-candidate source skip governance", false);
        }
        String content = safeText(document.contentText());
        String sourceUrl = normalizeUrl(document.sourceUrl());
        double rerankScore = 1.0D;
        double retrievalScore = 1.0D;
        double duplicateSimilarity = nearestFormalSimilarity(userId, projectId, content);
        boolean approvedUrlAbsent = !kbDocumentRepository.existsActiveSourceUrl(userId, projectId, FORMAL_COLLECTION, sourceUrl);
        boolean domainWhitelisted = isWhitelisted(sourceUrl);
        boolean contentEnough = content.length() >= kbProperties.getGovernance().getMinContentLength();
        boolean rerankPass = rerankScore >= kbProperties.getGovernance().getMinRerankScore();
        boolean retrievalPass = retrievalScore >= kbProperties.getGovernance().getMinRetrievalScore();
        boolean duplicatePass = duplicateSimilarity < kbProperties.getGovernance().getMaxDuplicateSimilarity();
        String reason = promotionReason(domainWhitelisted, rerankPass, retrievalPass, contentEnough, approvedUrlAbsent, duplicatePass, duplicateSimilarity);
        if (!(domainWhitelisted && rerankPass && retrievalPass && contentEnough && approvedUrlAbsent && duplicatePass)) {
            return new GovernanceResult("CANDIDATE_PENDING", "", reason, false);
        }
        Optional<KbCandidateRecord> existing = repository.list(Map.of("documentId", documentId, "size", "1")).list().stream().findFirst();
        if (existing.isPresent() && existing.get().approvedDocumentId() != null && !existing.get().approvedDocumentId().isBlank()) {
            return new GovernanceResult(existing.get().promotionStatus(), existing.get().approvedDocumentId(), existing.get().promotionReason(), false);
        }
        String approvedDocumentId = promoteToFormal(userId, projectId,
                new WebSearchResult(document.title(), sourceUrl, content, LocalDateTime.now(), retrievalScore),
                content);
        return new GovernanceResult("AUTO_PROMOTED_ACTIVE", approvedDocumentId, reason, false);
    }

    @Override
    public List<KbCandidateRecord> recordCandidates(String userId, String projectId, String query, List<WebSearchResult> results, Map<String, Double> rerankScores) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        Map<String, Double> scores = rerankScores == null || rerankScores.isEmpty()
                ? rerankByQuery(query, results)
                : rerankScores;
        List<KbCandidateRecord> saved = new ArrayList<>();
        for (WebSearchResult result : results) {
            String content = safeText(result.summary());
            KbDocumentWriteVO candidateDocument = kbDocumentService.createDocument(null, new KbDocumentCreateRequest(
                    safeTitle(result.title()),
                    "web",
                    safe(projectId),
                    null,
                    "url",
                    safe(result.url()),
                    content,
                    CANDIDATE_COLLECTION,
                    true
            ));
            String candidateId = UUID.randomUUID().toString();
            double rerankScore = scores.getOrDefault(result.url(), 0D);
            double retrievalScore = normalizeScore(result.confidence());
            double duplicateSimilarity = nearestFormalSimilarity(userId, projectId, content);
            boolean approvedUrlAbsent = !kbDocumentRepository.existsActiveSourceUrl(userId, projectId, FORMAL_COLLECTION, normalizeUrl(result.url()));
            boolean domainWhitelisted = isWhitelisted(result.url());
            boolean contentEnough = content.length() >= kbProperties.getGovernance().getMinContentLength();
            boolean rerankPass = rerankScore >= kbProperties.getGovernance().getMinRerankScore();
            boolean retrievalPass = retrievalScore >= kbProperties.getGovernance().getMinRetrievalScore();
            boolean duplicatePass = duplicateSimilarity < kbProperties.getGovernance().getMaxDuplicateSimilarity();
            String promotionStatus = domainWhitelisted && rerankPass && retrievalPass && contentEnough && approvedUrlAbsent && duplicatePass
                    ? "AUTO_PROMOTED_ACTIVE"
                    : "CANDIDATE_PENDING";
            String approvedDocumentId = "";
            if ("AUTO_PROMOTED_ACTIVE".equals(promotionStatus)) {
                approvedDocumentId = promoteToFormal(userId, projectId, result, content);
            }
            KbCandidateRecord record = new KbCandidateRecord(
                    candidateId,
                    candidateDocument.document().id(),
                    safe(userId),
                    safe(projectId),
                    normalizeUrl(result.url()),
                    extractDomain(result.url()),
                    safeTitle(result.title()),
                    result.fetchedAt() == null ? LocalDateTime.now() : result.fetchedAt(),
                    rerankScore,
                    retrievalScore,
                    content.length(),
                    dedupeHash(content),
                    "CANDIDATE_PENDING",
                    promotionStatus,
                    promotionReason(domainWhitelisted, rerankPass, retrievalPass, contentEnough, approvedUrlAbsent, duplicatePass, duplicateSimilarity),
                    "",
                    "",
                    approvedDocumentId,
                    null,
                    content
            );
            repository.save(record);
            saved.add(record);
        }
        return saved;
    }

    @Override
    public PageResponse<KbCandidateRecord> listCandidates(Map<String, String> query) {
        return repository.list(query == null ? Map.of() : query);
    }

    @Override
    public Optional<KbCandidateRecord> findCandidate(String candidateId) {
        return repository.findById(candidateId);
    }

    @Override
    public KbCandidateRecord approve(String candidateId, String reviewerId, String comment) {
        KbCandidateRecord current = load(candidateId);
        String approvedDocumentId = current.approvedDocumentId();
        if (approvedDocumentId == null || approvedDocumentId.isBlank()) {
            approvedDocumentId = promoteToFormal(current.tenantUserId(), current.projectId(),
                    new WebSearchResult(current.sourceTitle(), current.sourceUrl(), current.contentText(), current.fetchTime(), current.retrievalScore()),
                    current.contentText());
        }
        KbCandidateRecord updated = new KbCandidateRecord(
                current.candidateId(),
                current.documentId(),
                current.tenantUserId(),
                current.projectId(),
                current.sourceUrl(),
                current.sourceDomain(),
                current.sourceTitle(),
                current.fetchTime(),
                current.rerankScore(),
                current.retrievalScore(),
                current.contentLength(),
                current.dedupeHash(),
                "CANDIDATE_PENDING",
                "MANUAL_APPROVED_ACTIVE",
                blank(comment) ? "manual approve" : comment,
                safe(reviewerId),
                safe(comment),
                approvedDocumentId,
                null,
                current.contentText()
        );
        repository.save(updated);
        return updated;
    }

    @Override
    public KbCandidateRecord reject(String candidateId, String reviewerId, String comment) {
        KbCandidateRecord current = load(candidateId);
        KbCandidateRecord updated = new KbCandidateRecord(
                current.candidateId(),
                current.documentId(),
                current.tenantUserId(),
                current.projectId(),
                current.sourceUrl(),
                current.sourceDomain(),
                current.sourceTitle(),
                current.fetchTime(),
                current.rerankScore(),
                current.retrievalScore(),
                current.contentLength(),
                current.dedupeHash(),
                "REJECTED",
                "REJECTED",
                blank(comment) ? "manual reject" : comment,
                safe(reviewerId),
                safe(comment),
                current.approvedDocumentId(),
                current.rolledBackAt(),
                current.contentText()
        );
        repository.save(updated);
        return updated;
    }

    @Override
    public KbCandidateRecord rollback(String candidateId, String reviewerId, String comment) {
        KbCandidateRecord current = load(candidateId);
        if (current.approvedDocumentId() != null && !current.approvedDocumentId().isBlank()) {
            kbDocumentRepository.findDocument(parseLong(current.approvedDocumentId())).ifPresent(document -> {
                chromaVectorStoreGateway.deleteByDocument(document.userId(), document.projectId(), document.collectionName(), current.approvedDocumentId());
                kbDocumentRepository.markRolledBack(document.id(), blank(comment) ? "rollback" : comment);
            });
        }
        KbCandidateRecord updated = new KbCandidateRecord(
                current.candidateId(),
                current.documentId(),
                current.tenantUserId(),
                current.projectId(),
                current.sourceUrl(),
                current.sourceDomain(),
                current.sourceTitle(),
                current.fetchTime(),
                current.rerankScore(),
                current.retrievalScore(),
                current.contentLength(),
                current.dedupeHash(),
                "ROLLED_BACK",
                "ROLLED_BACK",
                blank(comment) ? "manual rollback" : comment,
                safe(reviewerId),
                safe(comment),
                current.approvedDocumentId(),
                LocalDateTime.now(),
                current.contentText()
        );
        repository.save(updated);
        return updated;
    }

    @Override
    public KbCandidateRecord replay(String candidateId, String reviewerId) {
        KbCandidateRecord current = load(candidateId);
        WebSearchResult result = new WebSearchResult(current.sourceTitle(), current.sourceUrl(), current.contentText(), LocalDateTime.now(), current.retrievalScore());
        Map<String, Double> score = Map.of(current.sourceUrl(), current.rerankScore());
        List<KbCandidateRecord> records = recordCandidates(current.tenantUserId(), current.projectId(), current.sourceTitle(), List.of(result), score);
        KbCandidateRecord replayed = records.getFirst();
        return new KbCandidateRecord(
                replayed.candidateId(),
                replayed.documentId(),
                replayed.tenantUserId(),
                replayed.projectId(),
                replayed.sourceUrl(),
                replayed.sourceDomain(),
                replayed.sourceTitle(),
                replayed.fetchTime(),
                replayed.rerankScore(),
                replayed.retrievalScore(),
                replayed.contentLength(),
                replayed.dedupeHash(),
                replayed.governanceStatus(),
                replayed.promotionStatus(),
                "replayed by " + safe(reviewerId),
                safe(reviewerId),
                "",
                replayed.approvedDocumentId(),
                replayed.rolledBackAt(),
                replayed.contentText()
        );
    }

    private String promoteToFormal(String userId, String projectId, WebSearchResult result, String content) {
        KbDocumentWriteVO formalDocument = kbDocumentService.createDocument(null, new KbDocumentCreateRequest(
                safeTitle(result.title()),
                "web",
                safe(projectId),
                null,
                "url",
                safe(result.url()),
                content,
                FORMAL_COLLECTION,
                true
        ));
        return formalDocument.document().id();
    }

    private Map<String, Double> rerankByQuery(String query, List<WebSearchResult> results) {
        List<String> documents = results.stream().map(item -> safeText(item.summary())).toList();
        RerankResponse response = rerankGateway.rerank(new RerankRequest(safe(query), documents, results.size(), Map.of()));
        Map<String, Double> scoreMap = new LinkedHashMap<>();
        for (RerankResult result : response.results()) {
            if (result.index() >= 0 && result.index() < results.size()) {
                scoreMap.put(results.get(result.index()).url(), result.score());
            }
        }
        return scoreMap;
    }

    private double nearestFormalSimilarity(String userId, String projectId, String content) {
        if (blank(content)) {
            return 0D;
        }
        List<Float> vector = embeddingGateway.embed(new EmbeddingRequest(List.of(content), Map.of("collection", FORMAL_COLLECTION))).vectors().stream()
                .findFirst()
                .orElse(List.of());
        if (vector.isEmpty()) {
            return 0D;
        }
        VectorSearchResponse response = chromaVectorStoreGateway.search(new VectorSearchRequest(
                safe(userId),
                safe(projectId),
                FORMAL_COLLECTION,
                "",
                vector,
                1,
                Map.of()
        ));
        return response.chunks().stream()
                .map(RetrievedChunk::score)
                .max(Comparator.naturalOrder())
                .orElse(0D);
    }

    private boolean isWhitelisted(String url) {
        String domain = extractDomain(url);
        String configured = kbProperties.getGovernance().getWhitelistDomains();
        if (blank(configured)) {
            return false;
        }
        return List.of(configured.split(",")).stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .anyMatch(item -> item.equalsIgnoreCase(domain));
    }

    private String promotionReason(boolean domainWhitelisted, boolean rerankPass, boolean retrievalPass,
                                   boolean contentEnough, boolean approvedUrlAbsent, boolean duplicatePass,
                                   double duplicateSimilarity) {
        List<String> reasons = new ArrayList<>();
        if (!domainWhitelisted) {
            reasons.add("domain_not_whitelisted");
        }
        if (!rerankPass) {
            reasons.add("low_rerank_score");
        }
        if (!retrievalPass) {
            reasons.add("low_retrieval_score");
        }
        if (!contentEnough) {
            reasons.add("content_too_short");
        }
        if (!approvedUrlAbsent) {
            reasons.add("formal_url_exists");
        }
        if (!duplicatePass) {
            reasons.add("duplicate_similarity=" + duplicateSimilarity);
        }
        return reasons.isEmpty() ? "auto-promoted" : String.join(",", reasons);
    }

    private KbCandidateRecord load(String candidateId) {
        return repository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("KB candidate not found: " + candidateId));
    }

    private String extractDomain(String url) {
        try {
            return URI.create(normalizeUrl(url)).getHost();
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private String normalizeUrl(String url) {
        return blank(url) ? "" : url.trim();
    }

    private String dedupeHash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(safeText(value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : hashed) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private String safeText(String value) {
        return blank(value) ? "" : value.trim();
    }

    private String safeTitle(String value) {
        return blank(value) ? "web-candidate" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private double normalizeScore(double value) {
        if (value < 0D) {
            return 0D;
        }
        if (value > 1D) {
            return 1D;
        }
        return value;
    }
}
