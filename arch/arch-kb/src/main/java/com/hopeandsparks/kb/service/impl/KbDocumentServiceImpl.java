package com.hopeandsparks.kb.service.impl;

import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.redis.RedisStreamClient;
import com.hopeandsparks.infra.redis.RedisStreamMessage;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.kb.dto.KbChunkCorrectRequest;
import com.hopeandsparks.kb.dto.KbDocumentCreateRequest;
import com.hopeandsparks.kb.dto.KbDocumentReparseRequest;
import com.hopeandsparks.kb.dto.KbDocumentUpdateRequest;
import com.hopeandsparks.kb.entity.KbChunkRecord;
import com.hopeandsparks.kb.entity.KbDocument;
import com.hopeandsparks.kb.entity.KbParseStrategy;
import com.hopeandsparks.kb.repository.KbDocumentRepository;
import com.hopeandsparks.kb.service.KbDocumentService;
import com.hopeandsparks.kb.vo.KbChunkCorrectResultVO;
import com.hopeandsparks.kb.vo.KbChunkVO;
import com.hopeandsparks.kb.vo.KbDocumentVO;
import com.hopeandsparks.kb.vo.KbDocumentWriteVO;
import com.hopeandsparks.kb.vo.KbParseStatusVO;
import com.hopeandsparks.task.dto.CreateAsyncTaskCommand;
import com.hopeandsparks.task.enums.AsyncTaskStatus;
import com.hopeandsparks.task.service.AsyncTaskService;
import com.hopeandsparks.task.vo.AsyncTaskVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * W7 KB implementation for Manage governance.
 */
@Service
public class KbDocumentServiceImpl implements KbDocumentService {

    private static final String PARSE_STREAM = "queue:kb:parse";
    private static final String PARSE_GROUP = "kb-parse";
    private static final long DEFAULT_PAGE = 1;
    private static final long DEFAULT_PAGE_SIZE = 20;
    private static final long MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_EMBEDDING_MODEL = "bge-m3-mock";

    private final KbDocumentRepository kbDocumentRepository;
    private final AsyncTaskService asyncTaskService;
    private final RedisStreamClient redisStreamClient;
    private final Map<Long, String> latestParseTaskIds = new ConcurrentHashMap<>();

    public KbDocumentServiceImpl(
            KbDocumentRepository kbDocumentRepository,
            AsyncTaskService asyncTaskService,
            RedisStreamClient redisStreamClient
    ) {
        this.kbDocumentRepository = kbDocumentRepository;
        this.asyncTaskService = asyncTaskService;
        this.redisStreamClient = redisStreamClient;
    }

    @Override
    public PageResponse<KbDocumentVO> listDocuments(Map<String, String> query) {
        long page = parseLong(value(query, "page"), DEFAULT_PAGE);
        long pageSize = Math.min(parseLong(value(query, "pageSize"), DEFAULT_PAGE_SIZE), MAX_PAGE_SIZE);
        long total = kbDocumentRepository.countDocuments(query);
        List<KbDocumentVO> list = kbDocumentRepository
                .listDocuments(query, (page - 1) * pageSize, pageSize)
                .stream()
                .map(this::toDocumentVO)
                .toList();
        return PageResponse.of(page, pageSize, total, list);
    }

    @Override
    @Transactional
    public KbDocumentWriteVO createDocument(AuthenticatedPrincipal principal, KbDocumentCreateRequest request) {
        Long adminId = requireAdmin(principal);
        if (request == null) {
            throw new BusinessException(400, "request body cannot be empty");
        }
        Long fileId = requireId(request.fileId(), "fileId format is invalid");
        if (!kbDocumentRepository.existsFile(fileId)) {
            throw new BusinessException(404, "uploaded file does not exist");
        }
        Long courseId = parseOptionalId(request.courseId());
        Long nodeId = parseOptionalId(request.nodeId());
        Long strategyId = parseOptionalId(request.parseStrategyId());
        validateRelations(courseId, nodeId, strategyId);

        Long documentId = kbDocumentRepository.insertDocument(
                requireText(request.kbDomain(), "kbDomain cannot be blank"),
                courseId,
                nodeId,
                requireText(request.title(), "title cannot be blank"),
                fileId,
                normalizeDocType(request.docType(), request.title()),
                normalizeSourceType(request.sourceType()),
                normalizeCollectionName(request.collectionName(), request.kbDomain()),
                strategyId,
                firstText(request.embeddingModel(), DEFAULT_EMBEDDING_MODEL),
                trimToNull(request.embeddingVersion()),
                adminId
        );
        KbDocument document = requireDocument(documentId);
        AsyncTaskVO task = queueParse(document, "create");
        return new KbDocumentWriteVO(toDocumentVO(document), task, "document created and parse task queued");
    }

    @Override
    @Transactional
    public KbDocumentWriteVO updateDocument(
            AuthenticatedPrincipal principal,
            String documentId,
            KbDocumentUpdateRequest request
    ) {
        requireAdmin(principal);
        Long parsedDocumentId = requireId(documentId, "documentId format is invalid");
        KbDocument oldDocument = requireDocument(parsedDocumentId);
        if (request == null) {
            throw new BusinessException(400, "request body cannot be empty");
        }
        Long courseId = request.courseId() == null ? oldDocument.courseId() : parseOptionalId(request.courseId());
        Long nodeId = request.nodeId() == null ? oldDocument.nodeId() : parseOptionalId(request.nodeId());
        Long strategyId = request.parseStrategyId() == null
                ? oldDocument.parseStrategyId()
                : parseOptionalId(request.parseStrategyId());
        validateRelations(courseId, nodeId, strategyId);

        kbDocumentRepository.updateDocument(
                oldDocument.id(),
                firstText(request.kbDomain(), oldDocument.kbDomain()),
                courseId,
                nodeId,
                firstText(request.title(), oldDocument.title()),
                firstText(request.docType(), oldDocument.docType()),
                firstText(request.sourceType(), oldDocument.sourceType()),
                firstText(request.collectionName(), oldDocument.collectionName()),
                strategyId,
                firstText(request.embeddingModel(), oldDocument.embeddingModel()),
                request.embeddingVersion() == null ? oldDocument.embeddingVersion() : trimToNull(request.embeddingVersion())
        );

        KbDocument updated = requireDocument(parsedDocumentId);
        AsyncTaskVO task = null;
        String message = "document metadata updated";
        if (Boolean.TRUE.equals(request.reparse())) {
            kbDocumentRepository.resetForParse(parsedDocumentId);
            updated = requireDocument(parsedDocumentId);
            task = queueParse(updated, "metadata updated");
            message = "document metadata updated and reparse task queued";
        }
        return new KbDocumentWriteVO(toDocumentVO(updated), task, message);
    }

    @Override
    @Transactional
    public KbDocumentWriteVO deleteDocument(AuthenticatedPrincipal principal, String documentId) {
        requireAdmin(principal);
        Long parsedDocumentId = requireId(documentId, "documentId format is invalid");
        KbDocument document = requireDocument(parsedDocumentId);
        kbDocumentRepository.markDocumentDeleted(parsedDocumentId);
        latestParseTaskIds.remove(parsedDocumentId);
        return new KbDocumentWriteVO(toDocumentVO(document), null, "document deleted");
    }

    @Override
    @Transactional
    public KbDocumentWriteVO reparseDocument(
            AuthenticatedPrincipal principal,
            String documentId,
            KbDocumentReparseRequest request
    ) {
        requireAdmin(principal);
        Long parsedDocumentId = requireId(documentId, "documentId format is invalid");
        requireDocument(parsedDocumentId);
        kbDocumentRepository.resetForParse(parsedDocumentId);
        KbDocument resetDocument = requireDocument(parsedDocumentId);
        String reason = request == null ? "manual reparse" : firstText(request.reason(), "manual reparse");
        AsyncTaskVO task = queueParse(resetDocument, reason);
        return new KbDocumentWriteVO(toDocumentVO(resetDocument), task, "reparse task queued");
    }

    @Override
    public KbParseStatusVO parseStatus(String documentId) {
        Long parsedDocumentId = requireId(documentId, "documentId format is invalid");
        KbDocument document = requireDocument(parsedDocumentId);
        AsyncTaskVO task = latestTask(parsedDocumentId).orElse(null);
        return new KbParseStatusVO(
                String.valueOf(document.id()),
                document.parseStatus(),
                document.chunkCount(),
                document.totalTokens(),
                vectorized(document),
                document.errorMsg(),
                task
        );
    }

    @Override
    public PageResponse<KbChunkVO> listChunks(String documentId, Map<String, String> query) {
        Long parsedDocumentId = requireId(documentId, "documentId format is invalid");
        requireDocument(parsedDocumentId);
        long page = parseLong(value(query, "page"), DEFAULT_PAGE);
        long pageSize = Math.min(parseLong(value(query, "pageSize"), DEFAULT_PAGE_SIZE), MAX_PAGE_SIZE);
        long total = kbDocumentRepository.countChunks(parsedDocumentId);
        List<KbChunkVO> list = kbDocumentRepository
                .listChunks(parsedDocumentId, (page - 1) * pageSize, pageSize)
                .stream()
                .map(this::toChunkVO)
                .toList();
        return PageResponse.of(page, pageSize, total, list);
    }

    @Override
    @Transactional
    public KbChunkCorrectResultVO correctChunk(
            AuthenticatedPrincipal principal,
            String chunkId,
            KbChunkCorrectRequest request
    ) {
        requireAdmin(principal);
        Long parsedChunkId = requireId(chunkId, "chunkId format is invalid");
        if (request == null || isBlank(request.correctedContent())) {
            throw new BusinessException(400, "correctedContent cannot be blank");
        }
        KbChunkRecord chunk = kbDocumentRepository.findChunkById(parsedChunkId)
                .orElseThrow(() -> new BusinessException(404, "chunk does not exist"));
        KbDocument document = requireDocument(chunk.documentId());

        AsyncTaskVO task = asyncTaskService.create(new CreateAsyncTaskCommand(
                "kb_chunk_reembed",
                "kb_chunk",
                String.valueOf(parsedChunkId),
                null,
                1
        ));
        asyncTaskService.start(task.taskId());
        kbDocumentRepository.updateParseStatus(document.id(), "embedding", null);
        String chromaPointId = firstText(chunk.chromaPointId(), chromaPointId(document.id(), document.documentVersion(), chunk.chunkIndex()));
        kbDocumentRepository.updateChunkContent(
                parsedChunkId,
                request.correctedContent().trim(),
                estimateTokens(request.correctedContent()),
                chromaPointId
        );
        kbDocumentRepository.refreshDocumentChunkStats(document.id());
        task = asyncTaskService.markSuccess(task.taskId(), "chunk corrected and mock vector updated");
        latestParseTaskIds.put(document.id(), task.taskId());
        KbDocument refreshed = requireDocument(document.id());
        return new KbChunkCorrectResultVO(
                String.valueOf(parsedChunkId),
                String.valueOf(document.id()),
                refreshed.parseStatus(),
                "chunk corrected",
                task
        );
    }

    @Override
    @Transactional
    public int consumePendingParseMessages() {
        int count = 0;
        for (RedisStreamMessage message : redisStreamClient.list(PARSE_STREAM)) {
            Map<String, String> body = message.body();
            if (!"kb_parse".equals(body.get("taskType"))) {
                continue;
            }
            parseDocument(body.get("documentId"), body.get("taskId"));
            redisStreamClient.ack(PARSE_STREAM, PARSE_GROUP, message.messageId());
            count++;
        }
        return count;
    }

    @Override
    @Transactional
    public void parseDocument(String documentId, String taskId) {
        Long parsedDocumentId = requireId(documentId, "documentId format is invalid");
        KbDocument document = requireDocument(parsedDocumentId);
        if (!isBlank(taskId) && asyncTaskService.findByTaskId(taskId)
                .filter(task -> task.status() == AsyncTaskStatus.SUCCESS || task.status() == AsyncTaskStatus.FAILED)
                .isPresent()) {
            return;
        }

        try {
            if (!isBlank(taskId)) {
                asyncTaskService.start(taskId);
                asyncTaskService.updateProgress(taskId, 25, "mock document parsing started");
            }
            kbDocumentRepository.updateParseStatus(parsedDocumentId, "parsing", null);

            List<String> chunks = mockParse(document);
            int totalTokens = 0;
            for (int i = 0; i < chunks.size(); i++) {
                String content = chunks.get(i);
                int tokenSize = estimateTokens(content);
                kbDocumentRepository.insertChunk(parsedDocumentId, i + 1, content, tokenSize);
                totalTokens += tokenSize;
            }

            if (!isBlank(taskId)) {
                asyncTaskService.updateProgress(taskId, 65, "mock embedding started");
            }
            kbDocumentRepository.updateParseStatus(parsedDocumentId, "embedding", null);
            for (int i = 0; i < chunks.size(); i++) {
                kbDocumentRepository.markChunkEmbedded(
                        parsedDocumentId,
                        i + 1,
                        chromaPointId(parsedDocumentId, document.documentVersion(), i + 1)
                );
            }
            kbDocumentRepository.updateParseResult(parsedDocumentId, totalTokens, chunks.size(), "success", null);
            if (!isBlank(taskId)) {
                asyncTaskService.markSuccess(taskId, "mock parse and vectorization completed");
            }
        } catch (RuntimeException exception) {
            kbDocumentRepository.updateParseStatus(parsedDocumentId, "failed", shortText(exception.getMessage(), 500));
            if (!isBlank(taskId)) {
                asyncTaskService.markFailed(taskId, shortText(exception.getMessage(), 500));
            }
            throw exception;
        }
    }

    private AsyncTaskVO queueParse(KbDocument document, String reason) {
        AsyncTaskVO task = asyncTaskService.create(new CreateAsyncTaskCommand(
                "kb_parse",
                "kb_document",
                String.valueOf(document.id()),
                "kb_parse:" + document.id() + ":v" + document.documentVersion(),
                2
        ));
        latestParseTaskIds.put(document.id(), task.taskId());
        Map<String, String> body = new LinkedHashMap<>();
        body.put("taskId", task.taskId());
        body.put("taskType", "kb_parse");
        body.put("documentId", String.valueOf(document.id()));
        body.put("documentVersion", String.valueOf(document.documentVersion()));
        body.put("reason", firstText(reason, "manual"));
        redisStreamClient.publish(PARSE_STREAM, body);
        return asyncTaskService.updateProgress(task.taskId(), 10, "kb parse message queued");
    }

    private List<String> mockParse(KbDocument document) {
        KbParseStrategy strategy = kbDocumentRepository.findStrategy(document.parseStrategyId())
                .orElse(new KbParseStrategy(null, "default-mock", 220, 30));
        int chunkSize = strategy.chunkSize() == null ? 220 : Math.max(strategy.chunkSize(), 80);
        String source = """
                Title: %s
                Domain: %s
                Type: %s
                Collection: %s
                Version: %s
                This is a deterministic mock parse result for W7 Manage governance. It keeps the document traceable,
                prepares chunks for RAG citation, and leaves real parser/vector provider integration behind infra gateways.
                """.formatted(
                document.title(),
                document.kbDomain(),
                document.docType(),
                document.collectionName(),
                document.documentVersion()
        ).replaceAll("\\s+", " ").trim();

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < source.length()) {
            int end = Math.min(start + chunkSize, source.length());
            chunks.add(source.substring(start, end).trim());
            start = end;
        }
        return chunks.isEmpty() ? List.of(document.title()) : chunks;
    }

    private void validateRelations(Long courseId, Long nodeId, Long strategyId) {
        if (!kbDocumentRepository.existsCourse(courseId)) {
            throw new BusinessException(404, "course does not exist");
        }
        if (!kbDocumentRepository.existsNode(nodeId)) {
            throw new BusinessException(404, "knowledge node does not exist");
        }
        if (strategyId != null && kbDocumentRepository.findStrategy(strategyId).isEmpty()) {
            throw new BusinessException(404, "parse strategy does not exist or is disabled");
        }
    }

    private KbDocument requireDocument(Long documentId) {
        return kbDocumentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(404, "kb document does not exist"));
    }

    private KbDocumentVO toDocumentVO(KbDocument document) {
        return new KbDocumentVO(
                String.valueOf(document.id()),
                document.kbDomain(),
                toStringId(document.courseId()),
                toStringId(document.nodeId()),
                document.title(),
                String.valueOf(document.fileId()),
                document.fileName(),
                document.fileType(),
                document.fileSize(),
                document.docType(),
                document.sourceType(),
                document.collectionName(),
                toStringId(document.parseStrategyId()),
                document.embeddingModel(),
                document.embeddingVersion(),
                document.documentVersion(),
                document.totalTokens(),
                document.chunkCount(),
                document.parseStatus(),
                vectorized(document),
                String.valueOf(document.uploaderId()),
                document.errorMsg(),
                document.createdAt(),
                document.updatedAt()
        );
    }

    private KbChunkVO toChunkVO(KbChunkRecord chunk) {
        return new KbChunkVO(
                String.valueOf(chunk.id()),
                String.valueOf(chunk.documentId()),
                chunk.chunkIndex(),
                chunk.contentText(),
                chunk.tokenSize(),
                chunk.chromaPointId(),
                embedStatus(chunk.embedStatus()),
                chunk.active(),
                chunk.createdAt(),
                chunk.updatedAt()
        );
    }

    private Optional<AsyncTaskVO> latestTask(Long documentId) {
        String taskId = latestParseTaskIds.get(documentId);
        return isBlank(taskId) ? Optional.empty() : asyncTaskService.findByTaskId(taskId);
    }

    private boolean vectorized(KbDocument document) {
        return "success".equals(document.parseStatus()) && document.chunkCount() != null && document.chunkCount() > 0;
    }

    private String embedStatus(Integer status) {
        if (status == null) {
            return "pending";
        }
        return switch (status) {
            case 1 -> "success";
            case 2 -> "failed";
            default -> "pending";
        };
    }

    private String chromaPointId(Long documentId, Integer version, Integer chunkIndex) {
        return "kb" + documentId + "v" + (version == null ? 1 : version) + "c" + chunkIndex;
    }

    private String normalizeDocType(String docType, String title) {
        if (!isBlank(docType)) {
            return docType.trim().toLowerCase(Locale.ROOT);
        }
        String safeTitle = title == null ? "" : title.toLowerCase(Locale.ROOT);
        if (safeTitle.endsWith(".pdf")) {
            return "pdf";
        }
        if (safeTitle.endsWith(".doc") || safeTitle.endsWith(".docx")) {
            return "word";
        }
        if (safeTitle.endsWith(".md") || safeTitle.endsWith(".markdown")) {
            return "md";
        }
        if (safeTitle.endsWith(".java") || safeTitle.endsWith(".py") || safeTitle.endsWith(".js")) {
            return "code";
        }
        return "doc";
    }

    private String normalizeSourceType(String sourceType) {
        String value = firstText(sourceType, "official").toLowerCase(Locale.ROOT);
        if ("user".equals(value) || "uploaded".equals(value) || "official".equals(value)) {
            return value;
        }
        return "official";
    }

    private String normalizeCollectionName(String collectionName, String kbDomain) {
        if (!isBlank(collectionName)) {
            return shortText(collectionName.trim(), 100);
        }
        String domain = firstText(kbDomain, "default").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (domain.isBlank()) {
            domain = "default";
        }
        return shortText("hope_kb_" + domain, 100);
    }

    private int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 1;
        }
        return Math.max(1, text.trim().length() / 2);
    }

    private Long requireAdmin(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.ADMIN) {
            throw new BusinessException(401, "please login with a manage admin account");
        }
        return principal.id();
    }

    private Long requireId(String value, String message) {
        Long id = parseOptionalId(value);
        if (id == null) {
            throw new BusinessException(400, message);
        }
        return id;
    }

    private Long parseOptionalId(String value) {
        if (isBlank(value)) {
            return null;
        }
        String text = value.trim();
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            int underline = text.lastIndexOf('_');
            if (underline >= 0 && underline < text.length() - 1) {
                try {
                    return Long.parseLong(text.substring(underline + 1));
                } catch (NumberFormatException ignoredAgain) {
                    return null;
                }
            }
            return null;
        }
    }

    private long parseLong(String value, long defaultValue) {
        Long parsed = parseOptionalId(value);
        return parsed == null ? defaultValue : Math.max(parsed, 1);
    }

    private String value(Map<String, String> query, String key) {
        return query == null ? null : query.get(key);
    }

    private String requireText(String value, String message) {
        if (isBlank(value)) {
            throw new BusinessException(400, message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
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

    private String toStringId(Long id) {
        return id == null ? null : String.valueOf(id);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
