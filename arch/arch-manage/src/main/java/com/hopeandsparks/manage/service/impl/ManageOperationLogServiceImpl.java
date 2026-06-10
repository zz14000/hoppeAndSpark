package com.hopeandsparks.manage.service.impl;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.manage.service.ManageOperationLogService;
import com.hopeandsparks.manage.vo.OperationLogVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ManageOperationLogServiceImpl implements ManageOperationLogService {

    private final List<OperationLogVO> logs = new ArrayList<>();

    @Override
    public void record(AuthenticatedPrincipal principal, String module, String action, String targetType, Long targetId, String detail, HttpServletRequest request) {
        logs.add(new OperationLogVO("log-" + System.currentTimeMillis(), module, action, targetType, targetId, detail, LocalDateTime.now(), true));
    }

    @Override
    public PageResponse<OperationLogVO> list(Map<String, String> query) {
        return PageResponse.of(1, 10, logs.size(), List.copyOf(logs));
    }
}
