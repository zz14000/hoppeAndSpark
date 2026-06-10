package com.hopeandsparks.kb.service.impl;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.chroma.ChromaVectorStoreGateway;
import com.hopeandsparks.infra.chroma.UpsertRequest;
import com.hopeandsparks.infra.chroma.VectorRecord;
import com.hopeandsparks.infra.config.AiProperties;
import com.hopeandsparks.infra.config.KbProperties;
import com.hopeandsparks.infra.embedding.EmbeddingGateway;
import com.hopeandsparks.infra.embedding.EmbeddingRequest;
import com.hopeandsparks.infra.embedding.EmbeddingResponse;
import com.hopeandsparks.infra.kb.ChunkedDocument;
import com.hopeandsparks.infra.kb.ChunkingService;
import com.hopeandsparks.infra.kb.DocumentChunk;
import com.hopeandsparks.infra.kb.DocumentParseRequest;
import com.hopeandsparks.infra.kb.DocumentParser;
import com.hopeandsparks.infra.kb.DocumentSourceType;
import com.hopeandsparks.infra.kb.ParsedDocument;
import com.hopeandsparks.infra.redis.RedisStreamClient;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.kb.dto.KbChunkCorrectRequest;
import com.hopeandsparks.kb.dto.KbDocumentCreateRequest;
import com.hopeandsparks.kb.dto.KbDocumentReparseRequest;
import com.hopeandsparks.kb.dto.KbDocumentUpdateRequest;
import com.hopeandsparks.kb.repository.KbChunkRecord;
import com.hopeandsparks.kb.repository.KbDocumentRecord;
import com.hopeandsparks.kb.repository.KbDocumentRepository;
import com.hopeandsparks.kb.service.KbDocumentService;
import com.hopeandsparks.kb.vo.KbChunkCorrectResultVO;
import com.hopeandsparks.kb.vo.KbChunkVO;
import com.hopeandsparks.kb.vo.KbDocumentVO;
import com.hopeandsparks.kb.vo.KbDocumentWriteVO;
import com.hopeandsparks.kb.vo.KbParseStatusVO;
import com.hopeandsparks.task.dto.RecordAsyncTaskEventCommand;
import com.hopeandsparks.task.service.AsyncTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class KbDocumentServiceImpl implements KbDocumentService {

    private final KbDocumentRepository repository;
    private final DocumentParser documentParser;
    private final ChunkingService chunkingService;
    private final EmbeddingGateway embeddingGateway;
    private final ChromaVectorStoreGateway chromaVectorStoreGateway;
    private final AiProperties aiProperties;
    private final KbProperties kbProperties;
    private final AsyncTaskService asyncTaskService;
    private final RedisStreamClient redisStreamClient;

    public KbDocumentServiceImpl(
            KbDocumentRepository repository,
            DocumentParser documentParser,
            ChunkingService chunkingService,
            EmbeddingGateway embeddingGateway,
            ChromaVectorStoreGateway chromaVectorStoreGateway,
            AiProperties aiProperties,
            KbProperties kbProperties,
            AsyncTaskService asyncTaskService,
            RedisStreamClient redisStreamClient
    ) {
        this.repository = repository;
        this.documentParser = documentParser;
        this.chunkingService = chunkingService;
        this.embeddingGateway = embeddingGateway;
        this.chromaVectorStoreGateway = chromaVectorStoreGateway;
        this.aiProperties = aiProperties;
        this.kbProperties = kbProperties;
        this.asyncTaskService = asyncTaskService;
        this.redisStreamClient = redisStreamClient;
    }

    @Override
    public PageResponse<KbDocumentVO> listDocuments(Map<String, String> query) {
        PageResponse<KbDocumentRecord> page = repository.listDocuments(query == null ? Map.of() : query);
        List<KbDocumentVO> items = page.list().stream().map(this::toDocumentVo).toList();
        return PageResponse.of(page.page(), page.pageSize(), page.total(), items);
    }

    @Override
    @Transactional
    public KbDocumentWriteVO createDocument(AuthenticatedPrincipal principal, KbDocumentCreateRequest request) {
        KbDocumentRecord record = new KbDocumentRecord(
                null,
                safe(request.domain(), "default"),
                safe(request.projectId(), "default"),
                safe(request.title(), "Untitled"),
                request.fileId(),
                detectDocType(request),
                safe(request.sourceType(), inferSourceType(request).name().toLowerCase()),
                request.sourceUrl(),
                request.contentText(),
                safe(request.collection(), "edu_ground_truth"),
                aiProperties.getEmbedding().getModel(),
                "v1",
                1,
                0,
                0,
                "pending",
                "",
                principal == null ? "0" : String.valueOf(principal.id()),
                false
        );
        Long documentId = repository.insertDocument(record);
        if (Boolean.TRUE.equals(request.parseNow())) {
            parseDocument(String.valueOf(documentId), "create");
        }
        return new KbDocumentWriteVO(loadDocument(documentId), "created", false);
    }

    @Override
    @Transactional
    public KbDocumentWriteVO updateDocument(AuthenticatedPrincipal principal, String documentId, KbDocumentUpdateRequest request) {
        Long id = parseDocumentId(documentId);
        KbDocumentRecord existing = repository.findDocument(id)
                .orElseThrow(() -> new IllegalArgumentException("KB document not found: " + documentId));
        KbDocumentRecord updated = new KbDocumentRecord(
                existing.id(),
                safe(request.domain(), existing.domain()),
                safe(request.projectId(), existing.projectId()),
                safe(request.title(), existing.title()),
                existing.fileId(),
                existing.docType(),
                existing.sourceType(),
                safe(request.sourceUrl(), existing.sourceUrl()),
                safe(request.contentText(), existing.contentText()),
                safe(request.collection(), existing.collectionName()),
                existing.embeddingModel(),
                existing.embeddingVersion(),
                existing.documentVersion() + 1,
                existing.totalTokens(),
                existing.chunkCount(),
                Boolean.TRUE.equals(request.reparse()) ? "pending" : existing.parseStatus(),
                "",
                existing.userId(),
                false
        );
        repository.updateDocument(id, updated);
        if (Boolean.TRUE.equals(request.reparse())) {
            parseDocument(documentId, "update");
        }
        return new KbDocumentWriteVO(loadDocument(id), "updated", false);
    }

    @Override
    @Transactional
    public KbDocumentWriteVO deleteDocument(AuthenticatedPrincipal principal, String documentId) {
        Long id = parseDocumentId(documentId);
        KbDocumentRecord existing = repository.findDocument(id)
                .orElseThrow(() -> new IllegalArgumentException("KB document not found: " + documentId));
        repository.markDeleted(id);
        chromaVectorStoreGateway.deleteByDocument(existing.userId(), existing.projectId(), existing.collectionName(), String.valueOf(id));
        return new KbDocumentWriteVO(toDeletedVo(existing), "deleted", false);
    }

    @Override
    @Transactional
    public KbDocumentWriteVO reparseDocument(AuthenticatedPrincipal principal, String documentId, KbDocumentReparseRequest request) {
        Long id = parseDocumentId(documentId);
        KbDocumentRecord existing = repository.findDocument(id)
                .orElseThrow(() -> new IllegalArgumentException("KB document not found: " + documentId));
        repository.updateParseResult(id, "pending", existing.totalTokens(), existing.chunkCount(), request == null ? "" : safe(request.reason(), ""));
        parseDocument(documentId, request == null ? "manual-reparse" : request.reason());
        return new KbDocumentWriteVO(loadDocument(id), "reparse queued", false);
    }

    @Override
    public KbParseStatusVO parseStatus(String documentId) {
        Long id = parseDocumentId(documentId);
        KbDocumentRecord record = repository.findDocument(id)
                .orElseThrow(() -> new IllegalArgumentException("KB document not found: " + documentId));
        int progress = switch (record.parseStatus()) {
            case "success" -> 100;
            case "embedding" -> 75;
            case "parsing" -> 30;
            case "failed" -> 100;
            default -> 0;
        };
        return new KbParseStatusVO(documentId, record.parseStatus(), progress, safe(record.errorMessage(), ""), false);
    }

    @Override
    public PageResponse<KbChunkVO> listChunks(String documentId, Map<String, String> query) {
        Long id = parseDocumentId(documentId);
        List<KbChunkVO> items = repository.listChunks(id).stream()
                .map(chunk -> new KbChunkVO(
                        String.valueOf(chunk.id()),
                        String.valueOf(chunk.documentId()),
                        chunk.chunkIndex(),
                        chunk.contentText(),
                        chunk.chromaPointId(),
                        chunk.sectionPath(),
                        false
                ))
                .toList();
        return PageResponse.of(1, items.size(), items.size(), items);
    }

    @Override
    @Transactional
    public KbChunkCorrectResultVO correctChunk(AuthenticatedPrincipal principal, String chunkId, KbChunkCorrectRequest request) {
        repository.updateChunkContent(parseDocumentId(chunkId), request.contentText());
        return new KbChunkCorrectResultVO(chunkId, "corrected", false);
    }

    @Override
    public int consumePendingParseMessages() {
        List<com.hopeandsparks.infra.redis.RedisStreamMessage> messages = redisStreamClient.list(kbProperties.getParse().getStreamKey());
        int handled = 0;
        for (com.hopeandsparks.infra.redis.RedisStreamMessage message : messages) {
            String taskId = message.body().getOrDefault("taskId", "");
            if (taskId.isBlank()) {
                continue;
            }
            String documentId = message.body().getOrDefault("documentId", "");
            String status = asyncTaskService.getByTaskId(taskId).status();
            if (!"QUEUED".equals(status) && !"RETRY_WAITING".equals(status)) {
                continue;
            }
            parseDocument(documentId, taskId);
            redisStreamClient.ack(kbProperties.getParse().getStreamKey(), kbProperties.getParse().getConsumerGroup(), message.messageId());
            handled++;
        }
        if (handled == 0) {
            List<KbDocumentRecord> pending = repository.loadPendingDocuments(kbProperties.getParse().getPendingBatchSize());
            pending.forEach(document -> parseDocument(String.valueOf(document.id()), "scheduler-fallback"));
            return pending.size();
        }
        return handled;
    }

    @Override
    @Transactional
    public void parseDocument(String documentId, String taskId) {
        Long id = parseDocumentId(documentId);
        KbDocumentRecord record = repository.findDocument(id)
                .orElseThrow(() -> new IllegalArgumentException("KB document not found: " + documentId));
        LocalDateTime taskStartedAt = LocalDateTime.now();
        if (hasTask(taskId)) {
            asyncTaskService.start(taskId);
        }
        repository.updateParseResult(id, "parsing", record.totalTokens(), record.chunkCount(), "");
        try {
            recordEvent(taskId, documentId, "PARSE", "STARTED", taskStartedAt, null, 0L, record.title(), "", "", "");
            ParsedDocument parsed = documentParser.parse(new DocumentParseRequest(
                    String.valueOf(id),
                    record.title(),
                    record.userId(),
                    record.projectId(),
                    record.domain(),
                    record.collectionName(),
                    inferSourceType(record),
                    record.fileId(),
                    record.sourceUrl(),
                    record.contentText()
            ));
            recordEvent(taskId, documentId, "PARSE", "SUCCESS", taskStartedAt, LocalDateTime.now(),
                    duration(taskStartedAt), record.title(), "parsedSections=" + parsed.sections().size(), "", "");
            LocalDateTime chunkStartedAt = LocalDateTime.now();
            ChunkedDocument chunked = chunkingService.chunk(parsed);
            recordEvent(taskId, documentId, "CHUNK", "SUCCESS", chunkStartedAt, LocalDateTime.now(),
                    duration(chunkStartedAt), "sections=" + parsed.sections().size(), "chunks=" + chunked.chunks().size(), "", "");
            List<String> texts = chunked.chunks().stream().map(DocumentChunk::text).toList();
            LocalDateTime embedStartedAt = LocalDateTime.now();
            EmbeddingResponse embedding = embeddingGateway.embed(new EmbeddingRequest(texts, Map.of(
                    "documentId", documentId,
                    "collection", record.collectionName()
            )));
            recordEvent(taskId, documentId, "EMBED", "SUCCESS", embedStartedAt, LocalDateTime.now(),
                    duration(embedStartedAt), "chunkCount=" + texts.size(), "vectorCount=" + embedding.vectors().size(), "", "");
            List<VectorRecord> records = new ArrayList<>();
            List<KbChunkRecord> chunkRecords = new ArrayList<>();
            int totalTokens = 0;
            for (int i = 0; i < chunked.chunks().size(); i++) {
                DocumentChunk chunk = chunked.chunks().get(i);
                totalTokens += chunk.tokenSize();
                String pointId = documentId + "-" + chunk.chunkIndex();
                Map<String, Object> metadata = new LinkedHashMap<>();
                metadata.put("documentId", documentId);
                metadata.put("chunkId", pointId);
                metadata.put("chunkIndex", chunk.chunkIndex());
                metadata.put("sectionPath", chunk.sectionPath());
                metadata.put("sourceTitle", record.title());
                metadata.put("sourceUrl", safe(record.sourceUrl(), ""));
                metadata.put("collection", record.collectionName());
                records.add(new VectorRecord(pointId, chunk.text(), embedding.vectors().get(i), metadata));
                chunkRecords.add(new KbChunkRecord(null, id, chunk.chunkIndex(), chunk.text(), chunk.tokenSize(), pointId, 1, true, chunk.sectionPath()));
            }
            LocalDateTime upsertStartedAt = LocalDateTime.now();
            chromaVectorStoreGateway.upsert(new UpsertRequest(record.userId(), record.projectId(), record.collectionName(), records));
            recordEvent(taskId, documentId, "VECTOR_UPSERT", "SUCCESS", upsertStartedAt, LocalDateTime.now(),
                    duration(upsertStartedAt), "records=" + records.size(), "collection=" + record.collectionName(), "", "");
            repository.replaceChunks(id, chunkRecords);
            repository.updateParseResult(id, "success", totalTokens, chunkRecords.size(), "");
            if (hasTask(taskId)) {
                asyncTaskService.markSuccess(taskId, "kb ingest success");
            }
            recordEvent(taskId, documentId, "DONE", "SUCCESS", taskStartedAt, LocalDateTime.now(),
                    duration(taskStartedAt), "documentVersion=" + record.documentVersion(), "chunkCount=" + chunkRecords.size(), "", "");
        } catch (RuntimeException exception) {
            repository.updateParseResult(id, "failed", record.totalTokens(), record.chunkCount(), exception.getMessage());
            if (hasTask(taskId)) {
                asyncTaskService.markFailed(taskId, exception.getMessage());
            }
            recordEvent(taskId, documentId, "DONE", "FAILED", taskStartedAt, LocalDateTime.now(),
                    duration(taskStartedAt), "documentVersion=" + record.documentVersion(), "", "KB_PARSE_FAILED", exception.getMessage());
            throw exception;
        }
    }

    private KbDocumentVO loadDocument(Long id) {
        return repository.findDocument(id)
                .map(this::toDocumentVo)
                .orElseThrow(() -> new IllegalArgumentException("KB document not found: " + id));
    }

    private KbDocumentVO toDeletedVo(KbDocumentRecord record) {
        return new KbDocumentVO(
                String.valueOf(record.id()),
                record.title(),
                record.domain(),
                record.sourceType(),
                record.sourceUrl(),
                record.fileId(),
                record.collectionName(),
                "deleted",
                false
        );
    }

    private KbDocumentVO toDocumentVo(KbDocumentRecord record) {
        return new KbDocumentVO(
                String.valueOf(record.id()),
                record.title(),
                record.domain(),
                record.sourceType(),
                record.sourceUrl(),
                record.fileId(),
                record.collectionName(),
                record.parseStatus(),
                false
        );
    }

    private DocumentSourceType inferSourceType(KbDocumentCreateRequest request) {
        String sourceType = safe(request.sourceType(), "");
        if (!sourceType.isBlank()) {
            return parseSourceType(sourceType);
        }
        if (request.fileId() != null && !request.fileId().isBlank()) {
            return DocumentSourceType.FILE;
        }
        if (request.sourceUrl() != null && request.sourceUrl().contains("video")) {
            return DocumentSourceType.VIDEO_URL;
        }
        if (request.sourceUrl() != null && !request.sourceUrl().isBlank()) {
            return DocumentSourceType.URL;
        }
        return DocumentSourceType.TEXT;
    }

    private DocumentSourceType inferSourceType(KbDocumentRecord record) {
        return parseSourceType(record.sourceType());
    }

    private DocumentSourceType parseSourceType(String value) {
        String normalized = safe(value, "text").replace('-', '_').toUpperCase();
        try {
            return DocumentSourceType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return DocumentSourceType.TEXT;
        }
    }

    private String detectDocType(KbDocumentCreateRequest request) {
        if (request.fileId() != null && !request.fileId().isBlank()) {
            return "file";
        }
        if (request.sourceUrl() != null && !request.sourceUrl().isBlank()) {
            return "url";
        }
        return "text";
    }

    private Long parseDocumentId(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid KB document id: " + value, exception);
        }
    }

    private String safe(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private boolean hasTask(String taskId) {
        return taskId != null && !taskId.isBlank() && asyncTaskService.findByTaskId(taskId).isPresent();
    }

    private void recordEvent(String taskId, String documentId, String stage, String status,
                             LocalDateTime startedAt, LocalDateTime finishedAt, long durationMs,
                             String inputSummary, String outputSummary, String errorCode, String errorMessage) {
        if (!hasTask(taskId)) {
            return;
        }
        int retryCount = asyncTaskService.getByTaskId(taskId).retryCount();
        asyncTaskService.recordEvent(new RecordAsyncTaskEventCommand(
                null,
                taskId,
                documentId,
                stage,
                status,
                startedAt,
                finishedAt,
                durationMs,
                safe(inputSummary, ""),
                safe(outputSummary, ""),
                safe(errorCode, ""),
                safe(errorMessage, ""),
                retryCount
        ));
    }

    private long duration(LocalDateTime startedAt) {
        return java.time.Duration.between(startedAt, LocalDateTime.now()).toMillis();
    }
}
