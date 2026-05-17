package com.hopeandsparks.interfaces.support;

import com.hopeandsparks.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 文件职责：为尚未接入真实 Application Service 的 Controller 统一生成 mock 响应，保证所有对外 API 先有稳定路由和统一响应结构。
 */
public final class MockApiResponseFactory {

    private MockApiResponseFactory() {
    }

    public static ApiResponse<Map<String, Object>> ok(
        HttpServletRequest request,
        String module,
        String action
    ) {
        return ok(request, module, action, Map.of(), Map.of(), null);
    }

    public static ApiResponse<Map<String, Object>> ok(
        HttpServletRequest request,
        String module,
        String action,
        Map<String, ?> pathVariables,
        Map<String, ?> query,
        Object body
    ) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("module", module);
        data.put("action", action);
        data.put("status", "mocked");
        data.put("pathVariables", pathVariables == null ? Map.of() : pathVariables);
        data.put("query", query == null ? Map.of() : query);
        data.put("body", body);
        data.put("next", "Connect this endpoint to its application service.");
        return ApiResponse.success(data, request.getAttribute("requestId").toString());
    }
}
