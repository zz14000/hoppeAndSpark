package com.hopeandsparks.manage.service;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.manage.vo.OperationLogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Operation log service used by Manage write actions.
 */
public interface ManageOperationLogService {

    void record(
            AuthenticatedPrincipal principal,
            String moduleName,
            String actionType,
            String targetType,
            Long targetId,
            String detail,
            HttpServletRequest request
    );

    PageResponse<OperationLogVO> list(Map<String, String> query);
}
