package com.hopeandsparks.interfaces.community;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接社区文章、草稿、润色、点赞、收藏、关注和评论接口，发布内容后续会进入异步审核。
 */
@RestController
@RequestMapping("/api/v1")
public class CommunityController {

    @GetMapping("/articles")
    public ApiResponse<Map<String, Object>> articles(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "articles", Map.of(), query, null);
    }

    @PostMapping("/articles")
    public ApiResponse<Map<String, Object>> publish(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "publishArticle", Map.of(), Map.of(), body);
    }

    @GetMapping("/articles/{articleId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String articleId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "articleDetail", Map.of("articleId", articleId), Map.of(), null);
    }

    @PostMapping("/articles/drafts")
    public ApiResponse<Map<String, Object>> draft(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "draft", Map.of(), Map.of(), body);
    }

    @PostMapping("/articles/polish")
    public ApiResponse<Map<String, Object>> polish(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "polish", Map.of(), Map.of(), body);
    }

    @PostMapping("/articles/{articleId}/like")
    public ApiResponse<Map<String, Object>> like(@PathVariable String articleId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "like", Map.of("articleId", articleId), Map.of(), null);
    }

    @DeleteMapping("/articles/{articleId}/like")
    public ApiResponse<Map<String, Object>> unlike(@PathVariable String articleId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "unlike", Map.of("articleId", articleId), Map.of(), null);
    }

    @PostMapping("/articles/{articleId}/collect")
    public ApiResponse<Map<String, Object>> collect(@PathVariable String articleId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "collect", Map.of("articleId", articleId), Map.of(), null);
    }

    @DeleteMapping("/articles/{articleId}/collect")
    public ApiResponse<Map<String, Object>> uncollect(@PathVariable String articleId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "uncollect", Map.of("articleId", articleId), Map.of(), null);
    }

    @PostMapping("/users/{userId}/follow")
    public ApiResponse<Map<String, Object>> follow(@PathVariable String userId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "follow", Map.of("userId", userId), Map.of(), null);
    }

    @DeleteMapping("/users/{userId}/follow")
    public ApiResponse<Map<String, Object>> unfollow(@PathVariable String userId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "unfollow", Map.of("userId", userId), Map.of(), null);
    }

    @GetMapping("/articles/{articleId}/comments")
    public ApiResponse<Map<String, Object>> comments(@PathVariable String articleId, @RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "comments", Map.of("articleId", articleId), query, null);
    }

    @PostMapping("/articles/{articleId}/comments")
    public ApiResponse<Map<String, Object>> comment(@PathVariable String articleId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "community", "comment", Map.of("articleId", articleId), Map.of(), body);
    }
}
