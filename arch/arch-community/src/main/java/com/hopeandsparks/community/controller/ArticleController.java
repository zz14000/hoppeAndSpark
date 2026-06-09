package com.hopeandsparks.community.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.community.dto.ArticleCommentRequest;
import com.hopeandsparks.community.dto.ArticleDraftRequest;
import com.hopeandsparks.community.dto.ArticlePolishRequest;
import com.hopeandsparks.community.dto.ArticlePublishRequest;
import com.hopeandsparks.community.service.ArticleService;
import com.hopeandsparks.community.vo.ArticleCardVO;
import com.hopeandsparks.community.vo.ArticleDetailVO;
import com.hopeandsparks.community.vo.ArticleDraftVO;
import com.hopeandsparks.community.vo.ArticlePolishVO;
import com.hopeandsparks.community.vo.ArticlePublishVO;
import com.hopeandsparks.community.vo.CommentPublishVO;
import com.hopeandsparks.community.vo.CommentVO;
import com.hopeandsparks.community.vo.ToggleResultVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Community article APIs.
 */
@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ArticleCardVO>> list(
            Authentication authentication,
            @RequestParam Map<String, String> query
    ) {
        return ApiResponse.ok(articleService.list(principal(authentication), query));
    }

    @PostMapping
    public ApiResponse<ArticlePublishVO> publish(
            Authentication authentication,
            @Valid @RequestBody ArticlePublishRequest request
    ) {
        return ApiResponse.ok(articleService.publish(principal(authentication), request));
    }

    @GetMapping("/{articleId}")
    public ApiResponse<ArticleDetailVO> detail(Authentication authentication, @PathVariable String articleId) {
        return ApiResponse.ok(articleService.detail(principal(authentication), articleId));
    }

    @PostMapping("/drafts")
    public ApiResponse<ArticleDraftVO> saveDraft(
            Authentication authentication,
            @Valid @RequestBody ArticleDraftRequest request
    ) {
        return ApiResponse.ok(articleService.saveDraft(principal(authentication), request));
    }

    @PostMapping("/polish")
    public ApiResponse<ArticlePolishVO> polish(
            Authentication authentication,
            @Valid @RequestBody ArticlePolishRequest request
    ) {
        return ApiResponse.ok(articleService.polish(principal(authentication), request));
    }

    @PostMapping("/{articleId}/like")
    public ApiResponse<ToggleResultVO> like(Authentication authentication, @PathVariable String articleId) {
        return ApiResponse.ok(articleService.like(principal(authentication), articleId));
    }

    @DeleteMapping("/{articleId}/like")
    public ApiResponse<ToggleResultVO> unlike(Authentication authentication, @PathVariable String articleId) {
        return ApiResponse.ok(articleService.unlike(principal(authentication), articleId));
    }

    @PostMapping("/{articleId}/collect")
    public ApiResponse<ToggleResultVO> collect(Authentication authentication, @PathVariable String articleId) {
        return ApiResponse.ok(articleService.collect(principal(authentication), articleId));
    }

    @DeleteMapping("/{articleId}/collect")
    public ApiResponse<ToggleResultVO> uncollect(Authentication authentication, @PathVariable String articleId) {
        return ApiResponse.ok(articleService.uncollect(principal(authentication), articleId));
    }

    @GetMapping("/{articleId}/comments")
    public ApiResponse<PageResponse<CommentVO>> comments(
            Authentication authentication,
            @PathVariable String articleId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize
    ) {
        return ApiResponse.ok(articleService.comments(principal(authentication), articleId, page, pageSize));
    }

    @PostMapping("/{articleId}/comments")
    public ApiResponse<CommentPublishVO> comment(
            Authentication authentication,
            @PathVariable String articleId,
            @Valid @RequestBody ArticleCommentRequest request
    ) {
        return ApiResponse.ok(articleService.comment(principal(authentication), articleId, request));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
