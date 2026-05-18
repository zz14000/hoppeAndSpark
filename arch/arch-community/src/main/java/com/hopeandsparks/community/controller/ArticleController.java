package com.hopeandsparks.community.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 社区文章接口，负责文章列表、发布、详情、草稿、AI 润色、点赞、收藏和评论。
 *
 * <p>文章和评论发布后会先进入审核状态，再通过 Redis Stream 异步审核后流转到 published。
 * 后续 Service 会写 {@code blog_post}、{@code blog_comment}、点赞和收藏等表。</p>
 */
@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("community", "articleList", values("query", query)));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> publish(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("community", "publishArticle", values("request", request)));
    }

    @GetMapping("/{articleId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String articleId) {
        return ApiResponse.ok(PlaceholderData.of("community", "articleDetail", values("articleId", articleId)));
    }

    @PostMapping("/drafts")
    public ApiResponse<Map<String, Object>> saveDraft(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("community", "saveDraft", values("request", request)));
    }

    @PostMapping("/polish")
    public ApiResponse<Map<String, Object>> polish(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("community", "polish", values("request", request)));
    }

    @PostMapping("/{articleId}/like")
    public ApiResponse<Map<String, Object>> like(@PathVariable String articleId) {
        return ApiResponse.ok(PlaceholderData.of("community", "like", values("articleId", articleId)));
    }

    @DeleteMapping("/{articleId}/like")
    public ApiResponse<Map<String, Object>> unlike(@PathVariable String articleId) {
        return ApiResponse.ok(PlaceholderData.of("community", "unlike", values("articleId", articleId)));
    }

    @PostMapping("/{articleId}/collect")
    public ApiResponse<Map<String, Object>> collect(@PathVariable String articleId) {
        return ApiResponse.ok(PlaceholderData.of("community", "collect", values("articleId", articleId)));
    }

    @DeleteMapping("/{articleId}/collect")
    public ApiResponse<Map<String, Object>> uncollect(@PathVariable String articleId) {
        return ApiResponse.ok(PlaceholderData.of("community", "uncollect", values("articleId", articleId)));
    }

    @GetMapping("/{articleId}/comments")
    public ApiResponse<Map<String, Object>> comments(@PathVariable String articleId, @RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("community", "comments", values("articleId", articleId, "query", query)));
    }

    @PostMapping("/{articleId}/comments")
    public ApiResponse<Map<String, Object>> comment(
            @PathVariable String articleId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("community", "comment", values("articleId", articleId, "request", request)));
    }
}
