package com.hopeandsparks.community.service;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.community.dto.ArticleCommentRequest;
import com.hopeandsparks.community.dto.ArticleDraftRequest;
import com.hopeandsparks.community.dto.ArticlePolishRequest;
import com.hopeandsparks.community.dto.ArticlePublishRequest;
import com.hopeandsparks.community.vo.ArticleCardVO;
import com.hopeandsparks.community.vo.ArticleDetailVO;
import com.hopeandsparks.community.vo.ArticleDraftVO;
import com.hopeandsparks.community.vo.ArticlePolishVO;
import com.hopeandsparks.community.vo.ArticlePublishVO;
import com.hopeandsparks.community.vo.CommentPublishVO;
import com.hopeandsparks.community.vo.CommentVO;
import com.hopeandsparks.community.vo.ToggleResultVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;

import java.util.Map;

/**
 * Community article use cases.
 */
public interface ArticleService {

    PageResponse<ArticleCardVO> list(AuthenticatedPrincipal principal, Map<String, String> query);

    ArticleDetailVO detail(AuthenticatedPrincipal principal, String articleId);

    ArticlePublishVO publish(AuthenticatedPrincipal principal, ArticlePublishRequest request);

    ArticleDraftVO saveDraft(AuthenticatedPrincipal principal, ArticleDraftRequest request);

    ArticlePolishVO polish(AuthenticatedPrincipal principal, ArticlePolishRequest request);

    ToggleResultVO like(AuthenticatedPrincipal principal, String articleId);

    ToggleResultVO unlike(AuthenticatedPrincipal principal, String articleId);

    ToggleResultVO collect(AuthenticatedPrincipal principal, String articleId);

    ToggleResultVO uncollect(AuthenticatedPrincipal principal, String articleId);

    PageResponse<CommentVO> comments(AuthenticatedPrincipal principal, String articleId, long page, long pageSize);

    CommentPublishVO comment(AuthenticatedPrincipal principal, String articleId, ArticleCommentRequest request);
}
