package com.hopeandsparks.manage.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 后台知识库管理接口，负责文档列表、上传、更新、删除和解析状态查询。
 *
 * <p>这里承接 {@code /api/v1/manage/knowledge-base/**} 后台入口，但不直接修改
 * {@code kb_document} 的核心状态。上传、删除、重解析等动作后续必须调用
 * {@code arch-kb} 的 Service；文件元数据仍通过 {@code arch-infra} 写入。</p>
 */
@RestController
@RequestMapping("/api/v1/manage/knowledge-base/documents")
public class ManageKnowledgeBaseController {

    @GetMapping
    public ApiResponse<Map<String, Object>> documents(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("manage", "kbDocuments", values("query", query)));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> uploadDocument(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "uploadKbDocument", values("request", request)));
    }

    @PutMapping("/{documentId}")
    public ApiResponse<Map<String, Object>> updateDocument(
            @PathVariable String documentId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("manage", "updateKbDocument", values("documentId", documentId, "request", request)));
    }

    @DeleteMapping("/{documentId}")
    public ApiResponse<Map<String, Object>> deleteDocument(@PathVariable String documentId) {
        return ApiResponse.ok(PlaceholderData.of("manage", "deleteKbDocument", values("documentId", documentId)));
    }

    @GetMapping("/{documentId}/parse-status")
    public ApiResponse<Map<String, Object>> parseStatus(@PathVariable String documentId) {
        return ApiResponse.ok(PlaceholderData.of("manage", "kbParseStatus", values("documentId", documentId)));
    }
}
