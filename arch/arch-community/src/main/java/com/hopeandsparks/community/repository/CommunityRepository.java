package com.hopeandsparks.community.repository;

import com.hopeandsparks.community.dto.ArticleQuery;
import com.hopeandsparks.community.entity.BlogComment;
import com.hopeandsparks.community.entity.BlogPost;
import com.hopeandsparks.community.entity.ModerationContent;
import com.hopeandsparks.community.enums.CommunityContentStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQL access for community articles, comments and user interactions.
 */
@Repository
public class CommunityRepository {

    private static final long ANONYMOUS_USER_ID = -1L;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<BlogPost> postMapper = (rs, rowNum) -> new BlogPost(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getString("nickname"),
            rs.getString("avatar_url"),
            rs.getString("title"),
            rs.getString("summary"),
            rs.getString("content_md"),
            rs.getObject("cover_file_id", Long.class),
            CommunityContentStatus.fromCode(rs.getInt("post_status")),
            rs.getInt("view_count"),
            rs.getInt("like_count"),
            rs.getInt("favorite_count"),
            rs.getLong("comment_count"),
            rs.getInt("liked") == 1,
            rs.getInt("collected") == 1,
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
    );

    private final RowMapper<BlogComment> commentMapper = (rs, rowNum) -> new BlogComment(
            rs.getLong("id"),
            rs.getLong("post_id"),
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getString("nickname"),
            rs.getString("avatar_url"),
            rs.getObject("parent_id", Long.class),
            rs.getObject("reply_to_user_id", Long.class),
            rs.getString("content"),
            CommunityContentStatus.fromCode(rs.getInt("comment_status")),
            rs.getInt("like_count"),
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
    );

    private final RowMapper<ModerationContent> moderationMapper = (rs, rowNum) -> new ModerationContent(
            rs.getString("target_type"),
            rs.getLong("target_id"),
            rs.getLong("user_id"),
            rs.getString("title"),
            rs.getString("content"),
            CommunityContentStatus.fromCode(rs.getInt("status_code"))
    );

    public CommunityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countArticles(ArticleQuery query, Long viewerId) {
        List<Object> args = new ArrayList<>();
        String sql = """
                select count(1)
                from blog_post p
                join sys_user u on u.id = p.user_id and u.is_deleted = 0
                where p.is_deleted = 0
                """ + articleFilters(query, viewerId, args);
        Long total = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    public List<BlogPost> listArticles(ArticleQuery query, Long viewerId, long offset, long pageSize) {
        List<Object> args = new ArrayList<>();
        addViewerArgs(args, viewerId);
        String sql = postSelect() + articleFilters(query, viewerId, args)
                + articleOrderBy(query)
                + " limit ? offset ?";
        args.add(pageSize);
        args.add(offset);
        return jdbcTemplate.query(sql, postMapper, args.toArray());
    }

    public Optional<BlogPost> findVisibleArticle(Long postId, Long viewerId) {
        try {
            List<Object> args = new ArrayList<>();
            addViewerArgs(args, viewerId);
            args.add(postId);
            if (viewerId == null) {
                args.add(CommunityContentStatus.PUBLISHED.code());
                return Optional.ofNullable(jdbcTemplate.queryForObject(postSelect() + """
                        and p.id = ?
                        and p.post_status = ?
                        """, postMapper, args.toArray()));
            }
            args.add(CommunityContentStatus.PUBLISHED.code());
            args.add(viewerId);
            args.add(CommunityContentStatus.DRAFT.code());
            args.add(CommunityContentStatus.PENDING.code());
            args.add(CommunityContentStatus.RISK.code());
            args.add(CommunityContentStatus.BLOCKED.code());
            return Optional.ofNullable(jdbcTemplate.queryForObject(postSelect() + """
                    and p.id = ?
                    and (
                        p.post_status = ?
                        or (p.user_id = ? and p.post_status in (?, ?, ?, ?))
                    )
                    """, postMapper, args.toArray()));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<BlogPost> findArticleForUser(Long postId, Long userId) {
        try {
            List<Object> args = new ArrayList<>();
            addViewerArgs(args, userId);
            args.add(postId);
            args.add(userId);
            return Optional.ofNullable(jdbcTemplate.queryForObject(postSelect() + """
                    and p.id = ?
                    and p.user_id = ?
                    """, postMapper, args.toArray()));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Long insertArticle(
            Long userId,
            String title,
            String summary,
            String content,
            Long coverFileId,
            CommunityContentStatus status
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into blog_post(user_id, title, summary, content_md, cover_file_id, post_status)
                    values (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, title);
            ps.setString(3, summary);
            ps.setString(4, content);
            if (coverFileId == null) {
                ps.setObject(5, null);
            } else {
                ps.setLong(5, coverFileId);
            }
            ps.setInt(6, status.code());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public void updateArticle(
            Long postId,
            Long userId,
            String title,
            String summary,
            String content,
            Long coverFileId,
            CommunityContentStatus status
    ) {
        jdbcTemplate.update("""
                update blog_post
                set title = ?,
                    summary = ?,
                    content_md = ?,
                    cover_file_id = ?,
                    post_status = ?
                where id = ? and user_id = ? and is_deleted = 0
                """, title, summary, content, coverFileId, status.code(), postId, userId);
    }

    public Optional<LocalDateTime> findArticleUpdatedAt(Long postId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select updated_at
                    from blog_post
                    where id = ? and is_deleted = 0
                    """, (rs, rowNum) -> toLocalDateTime(rs.getTimestamp("updated_at")), postId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public void recordView(Long postId, Long viewerId) {
        jdbcTemplate.update("""
                insert into blog_view_log(post_id, user_id, view_date)
                values (?, ?, current_date)
                """, postId, viewerId);
        jdbcTemplate.update("""
                update blog_post
                set view_count = view_count + 1
                where id = ? and is_deleted = 0
                """, postId);
    }

    public Long insertComment(
            Long postId,
            Long userId,
            Long parentId,
            Long replyToUserId,
            String content,
            CommunityContentStatus status
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into blog_comment(post_id, user_id, parent_id, reply_to_user_id, content, comment_status)
                    values (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, postId);
            ps.setLong(2, userId);
            ps.setObject(3, parentId);
            ps.setObject(4, replyToUserId);
            ps.setString(5, content);
            ps.setInt(6, status.code());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public Optional<BlogComment> findComment(Long commentId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(commentSelect() + """
                    and c.id = ?
                    """, commentMapper, commentId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public long countComments(Long postId, Long viewerId) {
        List<Object> args = new ArrayList<>();
        args.add(postId);
        String sql = """
                select count(1)
                from blog_comment c
                where c.post_id = ? and c.is_deleted = 0
                """ + commentVisibilityFilter(viewerId, args);
        Long total = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    public List<BlogComment> listComments(Long postId, Long viewerId, long offset, long pageSize) {
        List<Object> args = new ArrayList<>();
        args.add(postId);
        String sql = commentSelect() + """
                and c.post_id = ?
                """ + commentVisibilityFilter(viewerId, args) + """
                order by c.created_at asc, c.id asc
                limit ? offset ?
                """;
        args.add(pageSize);
        args.add(offset);
        return jdbcTemplate.query(sql, commentMapper, args.toArray());
    }

    public int likePost(Long userId, Long postId) {
        jdbcTemplate.update("""
                insert ignore into blog_like(user_id, target_type, target_id)
                values (?, 'post', ?)
                """, userId, postId);
        return refreshPostLikeCount(postId);
    }

    public int unlikePost(Long userId, Long postId) {
        jdbcTemplate.update("""
                delete from blog_like
                where user_id = ? and target_type = 'post' and target_id = ?
                """, userId, postId);
        return refreshPostLikeCount(postId);
    }

    public int collectPost(Long userId, Long postId) {
        jdbcTemplate.update("""
                insert ignore into blog_favorite(user_id, post_id)
                values (?, ?)
                """, userId, postId);
        return refreshPostFavoriteCount(postId);
    }

    public int uncollectPost(Long userId, Long postId) {
        jdbcTemplate.update("""
                delete from blog_favorite
                where user_id = ? and post_id = ?
                """, userId, postId);
        return refreshPostFavoriteCount(postId);
    }

    public void followUser(Long userId, Long targetUserId) {
        jdbcTemplate.update("""
                insert into user_friend(user_id, friend_user_id, friend_status, is_deleted)
                values (?, ?, 1, 0)
                on duplicate key update
                    friend_status = 1,
                    is_deleted = 0
                """, userId, targetUserId);
    }

    public void unfollowUser(Long userId, Long targetUserId) {
        jdbcTemplate.update("""
                update user_friend
                set is_deleted = 1
                where user_id = ? and friend_user_id = ?
                """, userId, targetUserId);
    }

    public boolean existsUser(Long userId) {
        Long total = jdbcTemplate.queryForObject("""
                select count(1)
                from sys_user
                where id = ? and is_deleted = 0 and account_status = 1
                """, Long.class, userId);
        return total != null && total > 0;
    }

    public Optional<ModerationContent> findModerationContent(String targetType, Long targetId) {
        try {
            if ("comment".equals(targetType)) {
                return Optional.ofNullable(jdbcTemplate.queryForObject("""
                        select 'comment' as target_type, c.id as target_id, c.user_id,
                               p.title, c.content, c.comment_status as status_code
                        from blog_comment c
                        join blog_post p on p.id = c.post_id and p.is_deleted = 0
                        where c.id = ? and c.is_deleted = 0
                        """, moderationMapper, targetId));
            }
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select 'post' as target_type, p.id as target_id, p.user_id,
                           p.title, p.content_md as content, p.post_status as status_code
                    from blog_post p
                    where p.id = ? and p.is_deleted = 0
                    """, moderationMapper, targetId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public boolean updateModerationStatus(String targetType, Long targetId, CommunityContentStatus status) {
        if ("comment".equals(targetType)) {
            int rows = jdbcTemplate.update("""
                    update blog_comment
                    set comment_status = ?
                    where id = ? and is_deleted = 0 and comment_status in (?, ?)
                    """, status.code(), targetId, CommunityContentStatus.PENDING.code(), CommunityContentStatus.RISK.code());
            return rows > 0;
        }
        int rows = jdbcTemplate.update("""
                update blog_post
                set post_status = ?
                where id = ? and is_deleted = 0 and post_status in (?, ?)
                """, status.code(), targetId, CommunityContentStatus.PENDING.code(), CommunityContentStatus.RISK.code());
        return rows > 0;
    }

    public Long insertModerationTicketIfAbsent(ModerationContent content, String issueType, String description) {
        Optional<Long> oldTicketId = findExistingTicketId(content.targetType(), content.targetId(), issueType);
        if (oldTicketId.isPresent()) {
            return oldTicketId.get();
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into feedback_ticket(user_id, target_type, target_id, issue_type,
                                                description, snapshot_content, status)
                    values (?, ?, ?, ?, ?, ?, 'pending')
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, content.userId());
            ps.setString(2, content.targetType());
            ps.setLong(3, content.targetId());
            ps.setString(4, issueType);
            ps.setString(5, description);
            ps.setString(6, content.title() + "\n\n" + content.content());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    private Optional<Long> findExistingTicketId(String targetType, Long targetId, String issueType) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id
                    from feedback_ticket
                    where target_type = ? and target_id = ? and issue_type = ?
                      and status = 'pending' and is_deleted = 0
                    order by id desc
                    limit 1
                    """, Long.class, targetType, targetId, issueType));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private int refreshPostLikeCount(Long postId) {
        jdbcTemplate.update("""
                update blog_post
                set like_count = (
                    select count(1)
                    from blog_like
                    where target_type = 'post' and target_id = ?
                )
                where id = ? and is_deleted = 0
                """, postId, postId);
        return countPostLikes(postId);
    }

    private int refreshPostFavoriteCount(Long postId) {
        jdbcTemplate.update("""
                update blog_post
                set favorite_count = (
                    select count(1)
                    from blog_favorite
                    where post_id = ?
                )
                where id = ? and is_deleted = 0
                """, postId, postId);
        return countPostFavorites(postId);
    }

    private int countPostLikes(Long postId) {
        Integer total = jdbcTemplate.queryForObject("""
                select count(1)
                from blog_like
                where target_type = 'post' and target_id = ?
                """, Integer.class, postId);
        return total == null ? 0 : total;
    }

    private int countPostFavorites(Long postId) {
        Integer total = jdbcTemplate.queryForObject("""
                select count(1)
                from blog_favorite
                where post_id = ?
                """, Integer.class, postId);
        return total == null ? 0 : total;
    }

    private String postSelect() {
        return """
                select p.id, p.user_id, u.username, u.nickname, u.avatar_url,
                       p.title, p.summary, p.content_md, p.cover_file_id,
                       p.post_status, p.view_count, p.like_count, p.favorite_count,
                       (
                           select count(1)
                           from blog_comment c
                           where c.post_id = p.id and c.is_deleted = 0 and c.comment_status = 1
                       ) as comment_count,
                       case when liked.id is null then 0 else 1 end as liked,
                       case when fav.id is null then 0 else 1 end as collected,
                       p.created_at, p.updated_at
                from blog_post p
                join sys_user u on u.id = p.user_id and u.is_deleted = 0
                left join blog_like liked
                  on liked.target_type = 'post' and liked.target_id = p.id and liked.user_id = ?
                left join blog_favorite fav
                  on fav.post_id = p.id and fav.user_id = ?
                where p.is_deleted = 0
                """;
    }

    private String commentSelect() {
        return """
                select c.id, c.post_id, c.user_id, u.username, u.nickname, u.avatar_url,
                       c.parent_id, c.reply_to_user_id, c.content, c.comment_status,
                       c.like_count, c.created_at, c.updated_at
                from blog_comment c
                join sys_user u on u.id = c.user_id and u.is_deleted = 0
                where c.is_deleted = 0
                """;
    }

    private String articleFilters(ArticleQuery query, Long viewerId, List<Object> args) {
        StringBuilder sql = new StringBuilder();
        if (query != null && query.authorId() != null) {
            sql.append(" and p.user_id = ?");
            args.add(query.authorId());
        }
        if (query != null && query.keyword() != null && !query.keyword().isBlank()) {
            String keyword = "%" + query.keyword().trim() + "%";
            sql.append(" and (p.title like ? or p.summary like ? or p.content_md like ?)");
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
        }
        if (query != null && "following".equalsIgnoreCase(query.category())) {
            if (viewerId == null) {
                sql.append(" and 1 = 0");
            } else {
                sql.append("""
                         and exists (
                             select 1
                             from user_friend uf
                             where uf.user_id = ? and uf.friend_user_id = p.user_id
                               and uf.friend_status = 1 and uf.is_deleted = 0
                         )
                        """);
                args.add(viewerId);
            }
        }
        appendArticleVisibility(sql, args, query, viewerId);
        return sql.toString();
    }

    private void appendArticleVisibility(StringBuilder sql, List<Object> args, ArticleQuery query, Long viewerId) {
        boolean ownAuthorQuery = viewerId != null && query != null && viewerId.equals(query.authorId());
        CommunityContentStatus requestedStatus = query == null ? null : CommunityContentStatus.fromApiValue(query.status());
        if (ownAuthorQuery) {
            if (requestedStatus != null) {
                sql.append(" and p.post_status = ?");
                args.add(requestedStatus.code());
                return;
            }
            sql.append(" and p.post_status in (?, ?, ?, ?, ?)");
            args.add(CommunityContentStatus.DRAFT.code());
            args.add(CommunityContentStatus.PUBLISHED.code());
            args.add(CommunityContentStatus.PENDING.code());
            args.add(CommunityContentStatus.RISK.code());
            args.add(CommunityContentStatus.BLOCKED.code());
            return;
        }
        sql.append(" and p.post_status = ?");
        args.add(CommunityContentStatus.PUBLISHED.code());
    }

    private String commentVisibilityFilter(Long viewerId, List<Object> args) {
        if (viewerId == null) {
            args.add(CommunityContentStatus.PUBLISHED.code());
            return " and c.comment_status = ?";
        }
        args.add(CommunityContentStatus.PUBLISHED.code());
        args.add(viewerId);
        args.add(CommunityContentStatus.PENDING.code());
        args.add(CommunityContentStatus.RISK.code());
        args.add(CommunityContentStatus.BLOCKED.code());
        return """
                 and (
                    c.comment_status = ?
                    or (c.user_id = ? and c.comment_status in (?, ?, ?))
                 )
                """;
    }

    private String articleOrderBy(ArticleQuery query) {
        String category = query == null ? null : query.category();
        if ("recommend".equalsIgnoreCase(category)) {
            return " order by p.like_count desc, p.favorite_count desc, p.updated_at desc, p.id desc";
        }
        return " order by p.created_at desc, p.id desc";
    }

    private void addViewerArgs(List<Object> args, Long viewerId) {
        args.add(viewerId == null ? ANONYMOUS_USER_ID : viewerId);
        args.add(viewerId == null ? ANONYMOUS_USER_ID : viewerId);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
