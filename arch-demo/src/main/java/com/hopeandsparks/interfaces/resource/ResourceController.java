package com.hopeandsparks.interfaces.resource;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接学习资源库、拓展阅读、代码案例、资源进度、导出和质量反馈接口。
 */
@RestController
@RequestMapping("/api/v1")
public class ResourceController {

    @GetMapping("/resources")
    public ApiResponse<Map<String, Object>> resources(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "resource", "list", Map.of(), query, null);
    }

    @GetMapping("/resources/{resourceId}")
    public ApiResponse<Map<String, Object>> resourceDetail(@PathVariable String resourceId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "resource", "detail", Map.of("resourceId", resourceId), Map.of(), null);
    }

    @GetMapping("/readings/{readingId}")
    public ApiResponse<Map<String, Object>> reading(@PathVariable String readingId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "resource", "reading", Map.of("readingId", readingId), Map.of(), null);
    }

    @GetMapping("/code-cases/{caseId}")
    public ApiResponse<Map<String, Object>> codeCase(@PathVariable String caseId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "resource", "codeCase", Map.of("caseId", caseId), Map.of(), null);
    }

    @PutMapping("/resources/{resourceId}/progress")
    public ApiResponse<Map<String, Object>> progress(@PathVariable String resourceId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "resource", "progress", Map.of("resourceId", resourceId), Map.of(), body);
    }

    @PostMapping("/resources/{resourceId}/export")
    public ApiResponse<Map<String, Object>> export(@PathVariable String resourceId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "resource", "export", Map.of("resourceId", resourceId), Map.of(), body);
    }

    @PostMapping("/resources/{resourceId}/feedback")
    public ApiResponse<Map<String, Object>> feedback(@PathVariable String resourceId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "resource", "feedback", Map.of("resourceId", resourceId), Map.of(), body);
    }
}
