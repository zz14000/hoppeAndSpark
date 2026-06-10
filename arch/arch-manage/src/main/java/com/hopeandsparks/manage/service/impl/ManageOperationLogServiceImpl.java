package com.hopeandsparks.manage.service.impl;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.manage.dto.OperationLogCommand;
import com.hopeandsparks.manage.entity.OperationLog;
import com.hopeandsparks.manage.repository.OperationLogRepository;
import com.hopeandsparks.manage.service.ManageOperationLogService;
import com.hopeandsparks.manage.vo.OperationLogVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Simple operation log implementation.
 */
@Service
public class ManageOperationLogServiceImpl implements ManageOperationLogService {

    private static final long DEFAULT_PAGE = 1;
    private static final long DEFAULT_PAGE_SIZE = 20;
    private static final long MAX_PAGE_SIZE = 100;

    private final OperationLogRepository operationLogRepository;

    public ManageOperationLogServiceImpl(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @Override
    public void record(
            AuthenticatedPrincipal principal,
            String moduleName,
            String actionType,
            String targetType,
            Long targetId,
            String detail,
            HttpServletRequest request
    ) {
        if (principal == null || principal.type() != IdentityType.ADMIN) {
            return;
        }
        operationLogRepository.insert(new OperationLogCommand(
                principal.id(),
                shortText(firstText(moduleName, "manage"), 50),
                shortText(firstText(actionType, "operate"), 30),
                shortText(firstText(targetType, "unknown"), 30),
                targetId,
                shortText(detail, 1000),
                shortText(clientIp(request), 50)
        ));
    }

    @Override
    public PageResponse<OperationLogVO> list(Map<String, String> query) {
        long page = parseLong(value(query, "page"), DEFAULT_PAGE);
        long pageSize = Math.min(parseLong(value(query, "pageSize"), DEFAULT_PAGE_SIZE), MAX_PAGE_SIZE);
        long total = operationLogRepository.count(query);
        List<OperationLogVO> list = operationLogRepository
                .list(query, (page - 1) * pageSize, pageSize)
                .stream()
                .map(this::toVO)
                .toList();
        return PageResponse.of(page, pageSize, total, list);
    }

    private OperationLogVO toVO(OperationLog log) {
        return new OperationLogVO(
                String.valueOf(log.id()),
                String.valueOf(log.adminId()),
                log.adminUsername(),
                log.moduleName(),
                log.actionType(),
                log.targetType(),
                log.targetId() == null ? null : String.valueOf(log.targetId()),
                log.detail(),
                log.ipAddress(),
                log.createdAt()
        );
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (!isBlank(forwarded)) {
            int comma = forwarded.indexOf(',');
            return comma >= 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
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
