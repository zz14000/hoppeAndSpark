package com.hopeandsparks.community.service.impl;

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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {
    public PageResponse<ArticleCardVO> list(AuthenticatedPrincipal principal, Map<String, String> query) {
        return PageResponse.of(1, 10, 1, List.of(new ArticleCardVO("mock-article", "Mock Article", "mock", true)));
    }
    public ArticlePublishVO publish(AuthenticatedPrincipal principal, ArticlePublishRequest request) {
        return new ArticlePublishVO("article-" + System.currentTimeMillis(), "published", true);
    }
    public ArticleDetailVO detail(AuthenticatedPrincipal principal, String articleId) {
        return new ArticleDetailVO(articleId, "Mock Article", "mock content", true);
    }
    public ArticleDraftVO saveDraft(AuthenticatedPrincipal principal, ArticleDraftRequest request) {
        return new ArticleDraftVO("draft-" + System.currentTimeMillis(), "saved", true);
    }
    public ArticlePolishVO polish(AuthenticatedPrincipal principal, ArticlePolishRequest request) {
        return new ArticlePolishVO(request.content() + "\n\nmock polished", true);
    }
    public ToggleResultVO like(AuthenticatedPrincipal principal, String articleId) { return new ToggleResultVO(articleId, true, true); }
    public ToggleResultVO unlike(AuthenticatedPrincipal principal, String articleId) { return new ToggleResultVO(articleId, false, true); }
    public ToggleResultVO collect(AuthenticatedPrincipal principal, String articleId) { return new ToggleResultVO(articleId, true, true); }
    public ToggleResultVO uncollect(AuthenticatedPrincipal principal, String articleId) { return new ToggleResultVO(articleId, false, true); }
    public PageResponse<CommentVO> comments(AuthenticatedPrincipal principal, String articleId, long page, long pageSize) {
        return PageResponse.of(page, pageSize, 0, List.of());
    }
    public CommentPublishVO comment(AuthenticatedPrincipal principal, String articleId, ArticleCommentRequest request) {
        return new CommentPublishVO("comment-" + System.currentTimeMillis(), "published", true);
    }
}
