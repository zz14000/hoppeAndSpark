package com.hopeandsparks.resource.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 文档资源接口，负责文档详情、目录、阅读进度和 Sage 伴读提问。
 *
 * <p>文档阅读进度属于学习记录，Sage 伴读会调用 Agent 能力，并可结合知识库切片做 RAG。
 * 后续这里会把资源内容、文件元数据、阅读状态和 Agent 问答串起来。</p>
 */
@RestController
public class DocumentController {

    @GetMapping("/api/v1/documents/{documentId}")
    public ApiResponse<Map<String, Object>> document(@PathVariable String documentId) {
        return ApiResponse.ok(PlaceholderData.of("resource", "document", values("documentId", documentId)));
    }

    @GetMapping("/api/v1/documents/{documentId}/outline")
    public ApiResponse<Map<String, Object>> outline(@PathVariable String documentId) {
        return ApiResponse.ok(PlaceholderData.of("resource", "outline", values("documentId", documentId)));
    }

    @PutMapping("/api/v1/documents/{documentId}/reading-progress")
    public ApiResponse<Map<String, Object>> readingProgress(
            @PathVariable String documentId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("resource", "readingProgress", values("documentId", documentId, "request", request)));
    }

    @PostMapping("/api/v1/documents/{documentId}/ask")
    public ApiResponse<Map<String, Object>> ask(
            @PathVariable String documentId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("resource", "documentAsk", values("documentId", documentId, "request", request)));
    }
}
