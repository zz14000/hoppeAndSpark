package com.hopeandsparks.community.service.impl;

import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.community.dto.ArticleCommentRequest;
import com.hopeandsparks.community.dto.ArticleDraftRequest;
import com.hopeandsparks.community.dto.ArticlePolishRequest;
import com.hopeandsparks.community.dto.ArticlePublishRequest;
import com.hopeandsparks.community.dto.ArticleQuery;
import com.hopeandsparks.community.entity.BlogComment;
import com.hopeandsparks.community.entity.BlogPost;
import com.hopeandsparks.community.entity.ModerationContent;
import com.hopeandsparks.community.enums.CommunityContentStatus;
import com.hopeandsparks.community.repository.CommunityRepository;
import com.hopeandsparks.community.service.ArticleService;
import com.hopeandsparks.community.service.CommunityModerationService;
import com.hopeandsparks.community.service.FollowService;
import com.hopeandsparks.community.vo.ArticleAuthorVO;
import com.hopeandsparks.community.vo.ArticleCardVO;
import com.hopeandsparks.community.vo.ArticleDetailVO;
import com.hopeandsparks.community.vo.ArticleDraftVO;
import com.hopeandsparks.community.vo.ArticlePolishVO;
import com.hopeandsparks.community.vo.ArticlePublishVO;
import com.hopeandsparks.community.vo.CommentPublishVO;
import com.hopeandsparks.community.vo.CommentVO;
import com.hopeandsparks.community.vo.FollowResultVO;
import com.hopeandsparks.community.vo.ToggleResultVO;
import com.hopeandsparks.infra.llm.LlmGateway;
import com.hopeandsparks.infra.llm.LlmRequest;
import com.hopeandsparks.infra.redis.RedisStreamClient;
import com.hopeandsparks.infra.redis.RedisStreamMessage;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.task.dto.CreateAsyncTaskCommand;
import com.hopeandsparks.task.enums.AsyncTaskStatus;
import com.hopeandsparks.task.service.AsyncTaskService;
import com.hopeandsparks.task.vo.AsyncTaskVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * W6 community implementation. It keeps the moderation state machine real and
 * leaves provider-specific AI work as deterministic mock logic for now.
 */
@Service
public class CommunityServiceImpl implements ArticleService, FollowService, CommunityModerationService {

    private static final String MODERATION_STREAM = "queue:community:moderation";
    private static final long DEFAULT_PAGE = 1;
    private static final long DEFAULT_PAGE_SIZE = 10;
    private static final long DEFAULT_COMMENT_PAGE_SIZE = 20;
    private static final long MAX_PAGE_SIZE = 100;

    private final CommunityRepository communityRepository;
    private final AsyncTaskService asyncTaskService;
    private final RedisStreamClient redisStreamClient;
    private final LlmGateway llmGateway;

    public CommunityServiceImpl(
            CommunityRepository communityRepository,
            AsyncTaskService asyncTaskService,
            RedisStreamClient redisStreamClient,
            LlmGateway llmGateway
    ) {
        this.communityRepository = communityRepository;
        this.asyncTaskService = asyncTaskService;
        this.redisStreamClient = redisStreamClient;
        this.llmGateway = llmGateway;
    }

    @Override
    public PageResponse<ArticleCardVO> list(AuthenticatedPrincipal principal, Map<String, String> query) {
        Long viewerId = optionalUserId(principal);
        long page = parseLong(value(query, "page"), DEFAULT_PAGE);
        long pageSize = Math.min(parseLong(value(query, "pageSize"), DEFAULT_PAGE_SIZE), MAX_PAGE_SIZE);
        ArticleQuery articleQuery = parseArticleQuery(query);
        long total = communityRepository.countArticles(articleQuery, viewerId);
        List<ArticleCardVO> list = communityRepository
                .listArticles(articleQuery, viewerId, (page - 1) * pageSize, pageSize)
                .stream()
                .map(this::toCardVO)
                .toList();
        return PageResponse.of(page, pageSize, total, list);
    }

    @Override
    @Transactional
    public ArticleDetailVO detail(AuthenticatedPrincipal principal, String articleId) {
        Long postId = requireId(articleId, "articleId format is invalid");
        Long viewerId = optionalUserId(principal);
        BlogPost post = communityRepository.findVisibleArticle(postId, viewerId)
                .orElseThrow(() -> new BusinessException(404, "article does not exist or is not visible"));
        boolean counted = post.postStatus().publicVisible();
        if (counted) {
            communityRepository.recordView(postId, viewerId);
        }
        return toDetailVO(post, counted);
    }

    @Override
    @Transactional
    public ArticlePublishVO publish(AuthenticatedPrincipal principal, ArticlePublishRequest request) {
        Long userId = requireUserId(principal);
        if (request == null) {
            throw new BusinessException(400, "request body cannot be empty");
        }
        Long coverFileId = parseOptionalId(request.coverFileId());
        Long postId = parseOptionalId(request.draftId());
        if (postId == null) {
            postId = communityRepository.insertArticle(
                    userId,
                    request.title().trim(),
                    trimToNull(request.summary()),
                    request.content(),
                    coverFileId,
                    CommunityContentStatus.PENDING
            );
        } else {
            BlogPost oldPost = communityRepository.findArticleForUser(postId, userId)
                    .orElseThrow(() -> new BusinessException(404, "draft article does not exist"));
            if (oldPost.postStatus() == CommunityContentStatus.OFFLINE) {
                throw new BusinessException(409, "offline article cannot be submitted again");
            }
            communityRepository.updateArticle(
                    postId,
                    userId,
                    request.title().trim(),
                    trimToNull(request.summary()),
                    request.content(),
                    coverFileId,
                    CommunityContentStatus.PENDING
            );
        }
        AsyncTaskVO task = createModerationTask("post", postId, userId);
        publishModerationMessage("post", postId, userId, task.taskId());
        task = asyncTaskService.updateProgress(task.taskId(), 10, "article moderation message has been queued");
        return new ArticlePublishVO(
                String.valueOf(postId),
                CommunityContentStatus.PENDING.apiValue(),
                task,
                "article is waiting for async moderation",
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public ArticleDraftVO saveDraft(AuthenticatedPrincipal principal, ArticleDraftRequest request) {
        Long userId = requireUserId(principal);
        if (request == null) {
            throw new BusinessException(400, "request body cannot be empty");
        }
        Long coverFileId = parseOptionalId(request.coverFileId());
        Long postId = parseOptionalId(request.draftId());
        if (postId == null) {
            postId = communityRepository.insertArticle(
                    userId,
                    request.title().trim(),
                    trimToNull(request.summary()),
                    request.content(),
                    coverFileId,
                    CommunityContentStatus.DRAFT
            );
        } else {
            BlogPost oldPost = communityRepository.findArticleForUser(postId, userId)
                    .orElseThrow(() -> new BusinessException(404, "draft article does not exist"));
            if (oldPost.postStatus() == CommunityContentStatus.PUBLISHED || oldPost.postStatus() == CommunityContentStatus.OFFLINE) {
                throw new BusinessException(409, "published or offline article cannot be overwritten as a draft");
            }
            communityRepository.updateArticle(
                    postId,
                    userId,
                    request.title().trim(),
                    trimToNull(request.summary()),
                    request.content(),
                    coverFileId,
                    CommunityContentStatus.DRAFT
            );
        }
        LocalDateTime updatedAt = communityRepository.findArticleUpdatedAt(postId).orElse(LocalDateTime.now());
        return new ArticleDraftVO(String.valueOf(postId), CommunityContentStatus.DRAFT.apiValue(), updatedAt);
    }

    @Override
    public ArticlePolishVO polish(AuthenticatedPrincipal principal, ArticlePolishRequest request) {
        requireUserId(principal);
        if (request == null || isBlank(request.content())) {
            throw new BusinessException(400, "content cannot be blank");
        }
        llmGateway.generate(new LlmRequest("polish community article", Map.of(
                "agent", firstText(request.agentId(), "horizon"),
                "tone", firstText(request.tone(), "friendly")
        )));
        String summary = buildSummary(request.content());
        List<String> tags = suggestTags(request.title(), request.content());
        String title = isBlank(request.title()) ? buildTitle(summary) : request.title().trim();
        String polished = polishText(request.content(), request.tone());
        ModerationDecision decision = decide(new ModerationContent(
                "post",
                0L,
                0L,
                title,
                request.content(),
                CommunityContentStatus.PENDING
        ));
        return new ArticlePolishVO(summary, tags, title, polished, riskLevel(decision.status()));
    }

    @Override
    @Transactional
    public ToggleResultVO like(AuthenticatedPrincipal principal, String articleId) {
        Long userId = requireUserId(principal);
        Long postId = requireId(articleId, "articleId format is invalid");
        requirePublishedArticle(postId, userId);
        int count = communityRepository.likePost(userId, postId);
        return new ToggleResultVO(String.valueOf(postId), "like", true, count);
    }

    @Override
    @Transactional
    public ToggleResultVO unlike(AuthenticatedPrincipal principal, String articleId) {
        Long userId = requireUserId(principal);
        Long postId = requireId(articleId, "articleId format is invalid");
        requirePublishedArticle(postId, userId);
        int count = communityRepository.unlikePost(userId, postId);
        return new ToggleResultVO(String.valueOf(postId), "like", false, count);
    }

    @Override
    @Transactional
    public ToggleResultVO collect(AuthenticatedPrincipal principal, String articleId) {
        Long userId = requireUserId(principal);
        Long postId = requireId(articleId, "articleId format is invalid");
        requirePublishedArticle(postId, userId);
        int count = communityRepository.collectPost(userId, postId);
        return new ToggleResultVO(String.valueOf(postId), "collect", true, count);
    }

    @Override
    @Transactional
    public ToggleResultVO uncollect(AuthenticatedPrincipal principal, String articleId) {
        Long userId = requireUserId(principal);
        Long postId = requireId(articleId, "articleId format is invalid");
        requirePublishedArticle(postId, userId);
        int count = communityRepository.uncollectPost(userId, postId);
        return new ToggleResultVO(String.valueOf(postId), "collect", false, count);
    }

    @Override
    public PageResponse<CommentVO> comments(AuthenticatedPrincipal principal, String articleId, long page, long pageSize) {
        Long postId = requireId(articleId, "articleId format is invalid");
        Long viewerId = optionalUserId(principal);
        communityRepository.findVisibleArticle(postId, viewerId)
                .orElseThrow(() -> new BusinessException(404, "article does not exist or is not visible"));
        long safePage = Math.max(page, 1);
        long safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        long total = communityRepository.countComments(postId, viewerId);
        List<CommentVO> list = communityRepository
                .listComments(postId, viewerId, (safePage - 1) * safePageSize, safePageSize)
                .stream()
                .map(this::toCommentVO)
                .toList();
        return PageResponse.of(safePage, safePageSize, total, list);
    }

    @Override
    @Transactional
    public CommentPublishVO comment(AuthenticatedPrincipal principal, String articleId, ArticleCommentRequest request) {
        Long userId = requireUserId(principal);
        Long postId = requireId(articleId, "articleId format is invalid");
        BlogPost post = requirePublishedArticle(postId, userId);
        if (request == null || isBlank(request.content())) {
            throw new BusinessException(400, "content cannot be blank");
        }
        Long parentId = parseOptionalId(request.parentId());
        Long replyToUserId = null;
        if (parentId != null) {
            BlogComment parent = communityRepository.findComment(parentId)
                    .orElseThrow(() -> new BusinessException(404, "parent comment does not exist"));
            if (!post.id().equals(parent.postId()) || !parent.commentStatus().publicVisible()) {
                throw new BusinessException(409, "parent comment is not available for reply");
            }
            replyToUserId = parent.userId();
        }

        Long commentId = communityRepository.insertComment(
                post.id(),
                userId,
                parentId,
                replyToUserId,
                request.content().trim(),
                CommunityContentStatus.PENDING
        );
        AsyncTaskVO task = createModerationTask("comment", commentId, userId);
        publishModerationMessage("comment", commentId, userId, task.taskId());
        task = asyncTaskService.updateProgress(task.taskId(), 10, "comment moderation message has been queued");
        return new CommentPublishVO(
                String.valueOf(commentId),
                String.valueOf(post.id()),
                CommunityContentStatus.PENDING.apiValue(),
                task,
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public FollowResultVO follow(AuthenticatedPrincipal principal, String userId) {
        Long currentUserId = requireUserId(principal);
        Long targetUserId = requireId(userId, "userId format is invalid");
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(409, "cannot follow yourself");
        }
        if (!communityRepository.existsUser(targetUserId)) {
            throw new BusinessException(404, "target user does not exist");
        }
        communityRepository.followUser(currentUserId, targetUserId);
        return new FollowResultVO(String.valueOf(targetUserId), true);
    }

    @Override
    @Transactional
    public FollowResultVO unfollow(AuthenticatedPrincipal principal, String userId) {
        Long currentUserId = requireUserId(principal);
        Long targetUserId = requireId(userId, "userId format is invalid");
        communityRepository.unfollowUser(currentUserId, targetUserId);
        return new FollowResultVO(String.valueOf(targetUserId), false);
    }

    @Override
    @Transactional
    public int consumePendingMessages() {
        int count = 0;
        for (RedisStreamMessage message : redisStreamClient.list(MODERATION_STREAM)) {
            Map<String, String> body = message.body();
            if (!"community_moderation".equals(body.get("taskType"))) {
                continue;
            }
            moderate(body.get("targetType"), body.get("targetId"), body.get("taskId"));
            redisStreamClient.ack(MODERATION_STREAM, "community-moderation", message.messageId());
            count++;
        }
        return count;
    }

    @Override
    @Transactional
    public void moderate(String targetType, String targetId, String taskId) {
        String safeTargetType = normalizeTargetType(targetType);
        Long parsedTargetId = requireId(targetId, "targetId format is invalid");
        if (!isBlank(taskId) && asyncTaskService.findByTaskId(taskId)
                .filter(task -> task.status() == AsyncTaskStatus.SUCCESS || task.status() == AsyncTaskStatus.FAILED)
                .isPresent()) {
            return;
        }
        if (!isBlank(taskId)) {
            asyncTaskService.start(taskId);
            asyncTaskService.updateProgress(taskId, 40, "mock moderation is running");
        }

        ModerationContent content = communityRepository.findModerationContent(safeTargetType, parsedTargetId)
                .orElseThrow(() -> new BusinessException(404, "moderation target does not exist"));
        if (!content.status().canBeModerated()) {
            if (!isBlank(taskId)) {
                asyncTaskService.markSuccess(taskId, "content was already moderated");
            }
            return;
        }

        ModerationDecision decision = decide(content);
        communityRepository.updateModerationStatus(safeTargetType, parsedTargetId, decision.status());
        if (decision.status() == CommunityContentStatus.RISK || decision.status() == CommunityContentStatus.BLOCKED) {
            communityRepository.insertModerationTicketIfAbsent(content, "inappropriate", decision.reason());
        }
        if (!isBlank(taskId)) {
            asyncTaskService.markSuccess(taskId, "moderation result: " + decision.status().apiValue());
        }
    }

    private AsyncTaskVO createModerationTask(String targetType, Long targetId, Long userId) {
        return asyncTaskService.create(new CreateAsyncTaskCommand(
                "community_moderation",
                targetType,
                String.valueOf(targetId),
                null,
                2
        ));
    }

    private void publishModerationMessage(String targetType, Long targetId, Long userId, String taskId) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("taskId", taskId);
        body.put("taskType", "community_moderation");
        body.put("targetType", targetType);
        body.put("targetId", String.valueOf(targetId));
        body.put("userId", String.valueOf(userId));
        redisStreamClient.publish(MODERATION_STREAM, body);
    }

    private ModerationDecision decide(ModerationContent content) {
        String text = (content.title() + " " + content.content()).toLowerCase(Locale.ROOT);
        if (containsAny(text, List.of("terror", "porn", "kill", "blocked_content"))) {
            return new ModerationDecision(CommunityContentStatus.BLOCKED, "mock moderation blocked severe content");
        }
        if (containsAny(text, List.of("risk", "spam", "illegal", "violence", "hate"))) {
            return new ModerationDecision(CommunityContentStatus.RISK, "mock moderation found risky content");
        }
        return new ModerationDecision(CommunityContentStatus.PUBLISHED, "mock moderation passed");
    }

    private BlogPost requirePublishedArticle(Long postId, Long viewerId) {
        BlogPost post = communityRepository.findVisibleArticle(postId, viewerId)
                .orElseThrow(() -> new BusinessException(404, "article does not exist or is not visible"));
        if (!post.postStatus().publicVisible()) {
            throw new BusinessException(409, "article is not published yet");
        }
        return post;
    }

    private ArticleQuery parseArticleQuery(Map<String, String> query) {
        return new ArticleQuery(
                value(query, "keyword"),
                value(query, "category"),
                value(query, "tag"),
                parseOptionalId(value(query, "authorId")),
                value(query, "status")
        );
    }

    private ArticleCardVO toCardVO(BlogPost post) {
        return new ArticleCardVO(
                String.valueOf(post.id()),
                post.title(),
                post.summary(),
                toAuthorVO(post.userId(), post.username(), post.nickname(), post.avatarUrl()),
                List.of(),
                post.postStatus().apiValue(),
                post.viewCount(),
                post.likeCount(),
                post.commentCount(),
                post.favoriteCount(),
                post.liked(),
                post.collected(),
                post.createdAt(),
                post.updatedAt()
        );
    }

    private ArticleDetailVO toDetailVO(BlogPost post, boolean counted) {
        return new ArticleDetailVO(
                String.valueOf(post.id()),
                post.title(),
                post.contentMd(),
                post.summary(),
                toAuthorVO(post.userId(), post.username(), post.nickname(), post.avatarUrl()),
                List.of(),
                post.coverFileId() == null ? null : String.valueOf(post.coverFileId()),
                post.postStatus().apiValue(),
                counted ? post.viewCount() + 1 : post.viewCount(),
                post.likeCount(),
                post.commentCount(),
                post.favoriteCount(),
                post.liked(),
                post.collected(),
                post.createdAt(),
                post.updatedAt()
        );
    }

    private CommentVO toCommentVO(BlogComment comment) {
        return new CommentVO(
                String.valueOf(comment.id()),
                String.valueOf(comment.postId()),
                toAuthorVO(comment.userId(), comment.username(), comment.nickname(), comment.avatarUrl()),
                comment.content(),
                comment.parentId() == null ? null : String.valueOf(comment.parentId()),
                comment.replyToUserId() == null ? null : String.valueOf(comment.replyToUserId()),
                comment.commentStatus().apiValue(),
                comment.likeCount(),
                comment.createdAt(),
                comment.updatedAt()
        );
    }

    private ArticleAuthorVO toAuthorVO(Long id, String username, String nickname, String avatarUrl) {
        return new ArticleAuthorVO(String.valueOf(id), firstText(nickname, username), avatarUrl);
    }

    private String buildSummary(String content) {
        String text = content == null ? "" : content.replaceAll("\\s+", " ").trim();
        if (text.length() <= 80) {
            return text;
        }
        return text.substring(0, 80) + "...";
    }

    private List<String> suggestTags(String title, String content) {
        List<String> tags = new ArrayList<>();
        String text = (firstText(title, "") + " " + firstText(content, "")).toLowerCase(Locale.ROOT);
        if (text.contains("java")) {
            tags.add("Java");
        }
        if (text.contains("spring")) {
            tags.add("Spring");
        }
        if (text.contains("vue")) {
            tags.add("Vue");
        }
        if (tags.isEmpty()) {
            tags.add("learning");
            tags.add("community");
        }
        return tags.stream().limit(3).toList();
    }

    private String buildTitle(String summary) {
        if (summary == null || summary.isBlank()) {
            return "Community article draft";
        }
        String shortText = summary.length() > 24 ? summary.substring(0, 24) : summary;
        return shortText;
    }

    private String polishText(String content, String tone) {
        String text = content == null ? "" : content.trim();
        if ("concise".equalsIgnoreCase(tone)) {
            return text.replaceAll("\\s+", " ");
        }
        return text;
    }

    private String riskLevel(CommunityContentStatus status) {
        if (status == CommunityContentStatus.PUBLISHED) {
            return "pass";
        }
        if (status == CommunityContentStatus.BLOCKED) {
            return "blocked";
        }
        return "risk";
    }

    private boolean containsAny(String text, List<String> words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeTargetType(String targetType) {
        if ("comment".equalsIgnoreCase(targetType)) {
            return "comment";
        }
        if ("post".equalsIgnoreCase(targetType) || "article".equalsIgnoreCase(targetType)) {
            return "post";
        }
        throw new BusinessException(400, "targetType must be post or comment");
    }

    private Long optionalUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            return null;
        }
        return principal.id();
    }

    private Long requireUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            throw new BusinessException(401, "please login with a frontend user account");
        }
        return principal.id();
    }

    private String value(Map<String, String> query, String key) {
        return query == null ? null : query.get(key);
    }

    private long parseLong(String value, long defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            return Math.max(Long.parseLong(value.trim()), 1);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private Long requireId(String value, String message) {
        Long id = parseOptionalId(value);
        if (id == null) {
            throw new BusinessException(400, message);
        }
        return id;
    }

    private Long parseOptionalId(String value) {
        if (isBlank(value)) {
            return null;
        }
        String text = value.trim();
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            int underline = text.lastIndexOf('_');
            if (underline >= 0 && underline < text.length() - 1) {
                try {
                    return Long.parseLong(text.substring(underline + 1));
                } catch (NumberFormatException ignoredAgain) {
                    return null;
                }
            }
            return null;
        }
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String firstText(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ModerationDecision(CommunityContentStatus status, String reason) {
    }
}
