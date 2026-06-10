package com.hopeandsparks.resource.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.resource.dto.ResourceExportRequest;
import com.hopeandsparks.resource.dto.ResourceFeedbackRequest;
import com.hopeandsparks.resource.dto.ResourceProgressUpdateRequest;
import com.hopeandsparks.resource.dto.ResourceQuery;
import com.hopeandsparks.resource.entity.LearningResource;
import com.hopeandsparks.resource.entity.LearningResourceVersion;
import com.hopeandsparks.resource.repository.ResourceRepository;
import com.hopeandsparks.resource.service.ResourceService;
import com.hopeandsparks.resource.vo.ResourceActionsVO;
import com.hopeandsparks.resource.vo.ResourceCardVO;
import com.hopeandsparks.resource.vo.ResourceDetailVO;
import com.hopeandsparks.resource.vo.ResourceExportVO;
import com.hopeandsparks.resource.vo.ResourceFeedbackVO;
import com.hopeandsparks.resource.vo.ResourceFileVO;
import com.hopeandsparks.resource.vo.ResourceProgressVO;
import com.hopeandsparks.resource.vo.ResourceVersionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final long DEFAULT_PAGE = 1;
    private static final long DEFAULT_PAGE_SIZE = 12;
    private static final long MAX_PAGE_SIZE = 100;

    private final ResourceRepository resourceRepository;
    private final ObjectMapper objectMapper;

    public ResourceServiceImpl(ResourceRepository resourceRepository, ObjectMapper objectMapper) {
        this.resourceRepository = resourceRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResponse<ResourceCardVO> listResources(AuthenticatedPrincipal principal, Map<String, String> query) {
        long page = parseLong(value(query, "page"), DEFAULT_PAGE);
        long pageSize = Math.min(parseLong(value(query, "pageSize"), DEFAULT_PAGE_SIZE), MAX_PAGE_SIZE);
        Long userId = optionalUserId(principal);
        ResourceQuery resourceQuery = new ResourceQuery(
                value(query, "type"),
                value(query, "keyword"),
                parseBoolean(value(query, "verified")),
                parseId(value(query, "planId")),
                parseId(value(query, "nodeId"))
        );
        long total = resourceRepository.countResources(resourceQuery);
        List<ResourceCardVO> list = resourceRepository
                .listResources(resourceQuery, (page - 1) * pageSize, pageSize, userId)
                .stream()
                .map(this::toCardVO)
                .toList();
        return PageResponse.of(page, pageSize, total, list);
    }

    @Override
    public ResourceDetailVO detail(AuthenticatedPrincipal principal, String resourceId) {
        Long parsedResourceId = requireId(resourceId, "资源ID格式不正确");
        LearningResource resource = resourceRepository.findById(parsedResourceId, optionalUserId(principal))
                .orElseThrow(() -> new BusinessException(404, "学习资源不存在"));
        return toDetailVO(resource, resourceRepository.listVersions(resource.id()));
    }

    @Override
    @Transactional
    public ResourceProgressVO updateProgress(
            AuthenticatedPrincipal principal,
            String resourceId,
            ResourceProgressUpdateRequest request
    ) {
        Long userId = requireUserId(principal);
        Long parsedResourceId = requireId(resourceId, "资源ID格式不正确");
        LearningResource resource = resourceRepository.findById(parsedResourceId, userId)
                .orElseThrow(() -> new BusinessException(404, "学习资源不存在"));

        int progress = normalizeProgress(request);
        int durationSeconds = normalizeDuration(request);
        String recordType = recordType(resource.resourceType());
        Long taskId = resourceRepository.findTaskIdForUserResource(userId, parsedResourceId).orElse(null);
        resourceRepository.insertLearningRecord(userId, taskId, parsedResourceId, recordType, durationSeconds);
        resourceRepository.updateTaskProgress(userId, parsedResourceId, progress);

        return new ResourceProgressVO(
                String.valueOf(parsedResourceId),
                progress,
                statusFromProgress(progress),
                recordType,
                LocalDateTime.now()
        );
    }

    @Override
    public ResourceExportVO exportResource(
            AuthenticatedPrincipal principal,
            String resourceId,
            ResourceExportRequest request
    ) {
        requireUserId(principal);
        Long parsedResourceId = requireId(resourceId, "资源ID格式不正确");
        LearningResource resource = resourceRepository.findById(parsedResourceId, principal.id())
                .orElseThrow(() -> new BusinessException(404, "学习资源不存在"));
        String format = request == null || request.format() == null || request.format().isBlank()
                ? "pdf"
                : request.format().trim().toLowerCase();
        String downloadUrl = resource.currentFileId() == null
                ? "/api/v1/resources/" + resource.id() + "/exported." + format
                : "/api/v1/files/" + resource.currentFileId();
        return new ResourceExportVO(String.valueOf(resource.id()), format, "mock_ready", downloadUrl);
    }

    @Override
    @Transactional
    public ResourceFeedbackVO feedback(
            AuthenticatedPrincipal principal,
            String resourceId,
            ResourceFeedbackRequest request
    ) {
        Long userId = requireUserId(principal);
        Long parsedResourceId = requireId(resourceId, "资源ID格式不正确");
        LearningResource resource = resourceRepository.findById(parsedResourceId, userId)
                .orElseThrow(() -> new BusinessException(404, "学习资源不存在"));
        String snapshot = toJson(Map.of(
                "resourceId", resource.id(),
                "title", resource.title(),
                "rating", request.rating() == null ? 0 : request.rating(),
                "evidence", request.evidence() == null ? List.of() : request.evidence()
        ));
        Long ticketId = resourceRepository.insertFeedbackTicket(
                userId,
                parsedResourceId,
                request.issueType(),
                request.content(),
                snapshot
        );
        return new ResourceFeedbackVO(ticketId == null ? null : String.valueOf(ticketId), "pending");
    }

    @Override
    public PageResponse<ResourceCardVO> listNodeResources(Long userId, Long nodeId, String type, long page, long pageSize) {
        long safePage = Math.max(page, 1);
        long safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        ResourceQuery query = new ResourceQuery(type, null, null, null, nodeId);
        long total = resourceRepository.countResources(query);
        List<ResourceCardVO> list = resourceRepository
                .listResources(query, (safePage - 1) * safePageSize, safePageSize, userId)
                .stream()
                .map(this::toCardVO)
                .toList();
        return PageResponse.of(safePage, safePageSize, total, list);
    }

    @Override
    public List<ResourceCardVO> listResourcesByNode(Long userId, Long nodeId, String type, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        ResourceQuery query = new ResourceQuery(type, null, null, null, nodeId);
        return resourceRepository.listResources(query, 0, safeLimit, userId)
                .stream()
                .map(this::toCardVO)
                .toList();
    }

    @Override
    public Optional<Long> firstResourceIdByNode(Long nodeId) {
        return resourceRepository.firstResourceIdByNode(nodeId);
    }

    @Override
    public long countResourcesByNode(Long nodeId) {
        return resourceRepository.countResourcesByNode(nodeId);
    }

    @Override
    public long countLearnedResourcesByNode(Long userId, Long nodeId) {
        return resourceRepository.countLearnedResourcesByNode(userId, nodeId);
    }

    @Override
    public long sumStudyDurationByNode(Long userId, Long nodeId) {
        return resourceRepository.sumStudyDurationByNode(userId, nodeId);
    }

    private ResourceCardVO toCardVO(LearningResource resource) {
        String type = apiType(resource);
        return new ResourceCardVO(
                String.valueOf(resource.id()),
                type,
                resource.title(),
                resource.summary(),
                safeDuration(resource),
                safeProgress(resource.progress()),
                statusFromProgress(safeProgress(resource.progress())),
                verifiedBy(resource.horizonCheckStatus()),
                tags(resource),
                detailRoute(type, resource.id()),
                detailApi(type, resource.id()),
                resource.collected()
        );
    }

    private ResourceDetailVO toDetailVO(LearningResource resource, List<LearningResourceVersion> versions) {
        String type = apiType(resource);
        int progress = safeProgress(resource.progress());
        return new ResourceDetailVO(
                String.valueOf(resource.id()),
                type,
                resource.title(),
                resource.summary(),
                String.valueOf(resource.nodeId()),
                resource.nodeCode(),
                resource.nodeName(),
                statusFromProgress(progress),
                progress,
                verifiedBy(resource.horizonCheckStatus()),
                detailRoute(type, resource.id()),
                detailApi(type, resource.id()),
                new ResourceActionsVO(true, true, canExport(type), true),
                toFileVO(resource),
                versions.stream().map(this::toVersionVO).toList(),
                tags(resource)
        );
    }

    private ResourceVersionVO toVersionVO(LearningResourceVersion version) {
        return new ResourceVersionVO(
                String.valueOf(version.id()),
                version.versionNo(),
                version.contentFileId() == null ? null : String.valueOf(version.contentFileId()),
                version.changeSummary(),
                horizonStatus(version.horizonCheckStatus()),
                version.createdAt()
        );
    }

    private ResourceFileVO toFileVO(LearningResource resource) {
        if (resource.currentFileId() == null) {
            return null;
        }
        return new ResourceFileVO(
                String.valueOf(resource.currentFileId()),
                resource.fileName(),
                resource.fileType(),
                "/api/v1/files/" + resource.currentFileId(),
                resource.fileSize()
        );
    }

    private String apiType(LearningResource resource) {
        String type = resource.resourceType() == null ? "document" : resource.resourceType().toLowerCase();
        return switch (type) {
            case "video" -> "video";
            case "quiz" -> "exercise_set";
            case "code" -> "code_case";
            case "mindmap" -> "mindmap";
            case "ppt" -> "document";
            case "doc" -> "text".equalsIgnoreCase(resource.contentSourceType()) ? "reading" : "document";
            default -> type;
        };
    }

    private String detailRoute(String type, Long resourceId) {
        return switch (type) {
            case "video" -> "/app/video?resourceId=" + resourceId;
            case "exercise_set" -> "/app/exercises/" + resourceId;
            case "reading" -> "/app/reading?resourceId=" + resourceId;
            case "code_case" -> "/app/code-case?resourceId=" + resourceId;
            case "mindmap" -> "/app/mindmap?resourceId=" + resourceId;
            default -> "/app/document?resourceId=" + resourceId;
        };
    }

    private String detailApi(String type, Long resourceId) {
        return switch (type) {
            case "video" -> "/api/v1/videos/" + resourceId;
            case "exercise_set" -> "/api/v1/practice/sets/" + resourceId;
            case "reading" -> "/api/v1/readings/" + resourceId;
            case "code_case" -> "/api/v1/code-cases/" + resourceId;
            default -> "/api/v1/documents/" + resourceId;
        };
    }

    private boolean canExport(String type) {
        return !"exercise_set".equals(type);
    }

    private List<String> tags(LearningResource resource) {
        return List.of(
                resource.nodeName() == null ? "知识点" : resource.nodeName(),
                resource.resourceLevel() == null ? "medium" : resource.resourceLevel(),
                resource.generatedBy() == null ? "Nebula" : resource.generatedBy()
        );
    }

    private Integer safeDuration(LearningResource resource) {
        return resource.durationSeconds() == null ? 0 : resource.durationSeconds();
    }

    private int safeProgress(Integer progress) {
        return progress == null ? 0 : Math.max(0, Math.min(progress, 100));
    }

    private String verifiedBy(Integer horizonCheckStatus) {
        return horizonCheckStatus != null && horizonCheckStatus == 1 ? "horizon" : null;
    }

    private String horizonStatus(Integer horizonCheckStatus) {
        if (horizonCheckStatus == null) {
            return "unchecked";
        }
        return switch (horizonCheckStatus) {
            case 1 -> "passed";
            case 2 -> "rejected";
            default -> "unchecked";
        };
    }

    private String statusFromProgress(int progress) {
        if (progress >= 100) {
            return "completed";
        }
        if (progress > 0) {
            return "learning";
        }
        return "not_started";
    }

    private String recordType(String resourceType) {
        String type = resourceType == null ? "" : resourceType.toLowerCase();
        return switch (type) {
            case "video" -> "play";
            case "quiz" -> "practice";
            case "doc", "ppt", "mindmap" -> "read";
            default -> "view";
        };
    }

    private int normalizeProgress(ResourceProgressUpdateRequest request) {
        if (request == null) {
            return 0;
        }
        if (Boolean.TRUE.equals(request.completed())) {
            return 100;
        }
        Integer value = request.progressPercent() == null ? request.progress() : request.progressPercent();
        return value == null ? 0 : Math.max(0, Math.min(value, 100));
    }

    private int normalizeDuration(ResourceProgressUpdateRequest request) {
        if (request == null) {
            return 0;
        }
        Integer value = request.durationSeconds();
        if (value == null) {
            value = request.positionSeconds() == null ? request.lastPosition() : request.positionSeconds();
        }
        return value == null ? 0 : Math.max(value, 0);
    }

    private Long requireUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            throw new BusinessException(401, "请先登录前台账号");
        }
        return principal.id();
    }

    private Long optionalUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            return null;
        }
        return principal.id();
    }

    private Long requireId(String value, String message) {
        Long parsed = parseId(value);
        if (parsed == null) {
            throw new BusinessException(400, message);
        }
        return parsed;
    }

    private Long parseId(String value) {
        String safeValue = value == null ? null : value.trim();
        if (safeValue == null || safeValue.isBlank()) {
            return null;
        }
        String digits = safeValue.matches("\\d+") ? safeValue : safeValue.replaceFirst("^.*_(\\d+)$", "$1");
        if (!digits.matches("\\d+")) {
            return null;
        }
        return Long.parseLong(digits);
    }

    private long parseLong(String value, long defaultValue) {
        Long parsed = parseId(value);
        return parsed == null ? defaultValue : Math.max(parsed, 1);
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String safeValue = value.trim().toLowerCase();
        if ("true".equals(safeValue) || "1".equals(safeValue) || "yes".equals(safeValue)) {
            return true;
        }
        if ("false".equals(safeValue) || "0".equals(safeValue) || "no".equals(safeValue)) {
            return false;
        }
        return null;
    }

    private String value(Map<String, String> query, String key) {
        return query == null ? null : query.get(key);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }
}
