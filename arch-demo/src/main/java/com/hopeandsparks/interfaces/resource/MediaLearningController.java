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
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接视频学习和文档阅读接口，后续会连接学习记录、字幕、伴读问答和阅读进度用例。
 */
@RestController
@RequestMapping("/api/v1")
public class MediaLearningController {

    @GetMapping("/videos/{videoId}")
    public ApiResponse<Map<String, Object>> video(@PathVariable String videoId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "media", "video", Map.of("videoId", videoId), Map.of(), null);
    }

    @GetMapping("/videos/{videoId}/episodes")
    public ApiResponse<Map<String, Object>> episodes(@PathVariable String videoId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "media", "episodes", Map.of("videoId", videoId), Map.of(), null);
    }

    @PutMapping("/videos/{videoId}/watch-progress")
    public ApiResponse<Map<String, Object>> watchProgress(@PathVariable String videoId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "media", "watchProgress", Map.of("videoId", videoId), Map.of(), body);
    }

    @GetMapping("/videos/{videoId}/transcripts")
    public ApiResponse<Map<String, Object>> transcripts(@PathVariable String videoId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "media", "transcripts", Map.of("videoId", videoId), Map.of(), null);
    }

    @GetMapping("/documents/{documentId}")
    public ApiResponse<Map<String, Object>> document(@PathVariable String documentId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "document", "detail", Map.of("documentId", documentId), Map.of(), null);
    }

    @GetMapping("/documents/{documentId}/outline")
    public ApiResponse<Map<String, Object>> outline(@PathVariable String documentId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "document", "outline", Map.of("documentId", documentId), Map.of(), null);
    }

    @PutMapping("/documents/{documentId}/reading-progress")
    public ApiResponse<Map<String, Object>> readingProgress(@PathVariable String documentId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "document", "readingProgress", Map.of("documentId", documentId), Map.of(), body);
    }

    @PostMapping("/documents/{documentId}/ask")
    public ApiResponse<Map<String, Object>> ask(@PathVariable String documentId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "document", "ask", Map.of("documentId", documentId), Map.of(), body);
    }
}
