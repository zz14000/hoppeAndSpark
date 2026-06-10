package com.hopeandsparks.kb.repository;

import com.hopeandsparks.common.response.PageResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class KbCandidateGovernanceRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<KbCandidateRecord> mapper = (rs, rowNum) -> new KbCandidateRecord(
            rs.getString("candidate_id"),
            rs.getString("document_id"),
            rs.getString("tenant_user_id"),
            rs.getString("project_id"),
            rs.getString("source_url"),
            rs.getString("source_domain"),
            rs.getString("source_title"),
            rs.getTimestamp("fetch_time") == null ? null : rs.getTimestamp("fetch_time").toLocalDateTime(),
            rs.getDouble("rerank_score"),
            rs.getDouble("retrieval_score"),
            rs.getInt("content_length"),
            rs.getString("dedupe_hash"),
            rs.getString("governance_status"),
            rs.getString("promotion_status"),
            rs.getString("promotion_reason"),
            rs.getString("reviewer_id"),
            rs.getString("review_comment"),
            rs.getString("approved_document_id"),
            rs.getTimestamp("rolled_back_at") == null ? null : rs.getTimestamp("rolled_back_at").toLocalDateTime(),
            rs.getString("content_text")
    );

    public KbCandidateGovernanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureSchema();
    }

    public void save(KbCandidateRecord record) {
        Integer count = jdbcTemplate.queryForObject("select count(1) from kb_candidate_governance where candidate_id = ?", Integer.class, record.candidateId());
        if (count != null && count > 0) {
            jdbcTemplate.update("""
                    update kb_candidate_governance
                    set document_id = ?, tenant_user_id = ?, project_id = ?, source_url = ?, source_domain = ?, source_title = ?,
                        fetch_time = ?, rerank_score = ?, retrieval_score = ?, content_length = ?, dedupe_hash = ?,
                        governance_status = ?, promotion_status = ?, promotion_reason = ?, reviewer_id = ?, review_comment = ?,
                        approved_document_id = ?, rolled_back_at = ?, content_text = ?
                    where candidate_id = ?
                    """,
                    record.documentId(), record.tenantUserId(), record.projectId(), record.sourceUrl(), record.sourceDomain(), record.sourceTitle(),
                    timestamp(record.fetchTime()), record.rerankScore(), record.retrievalScore(), record.contentLength(), record.dedupeHash(),
                    record.governanceStatus(), record.promotionStatus(), record.promotionReason(), record.reviewerId(), record.reviewComment(),
                    record.approvedDocumentId(), timestamp(record.rolledBackAt()), record.contentText(), record.candidateId());
        } else {
            jdbcTemplate.update("""
                    insert into kb_candidate_governance(candidate_id, document_id, tenant_user_id, project_id, source_url, source_domain, source_title,
                                                        fetch_time, rerank_score, retrieval_score, content_length, dedupe_hash, governance_status,
                                                        promotion_status, promotion_reason, reviewer_id, review_comment, approved_document_id, rolled_back_at, content_text)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    record.candidateId(), record.documentId(), record.tenantUserId(), record.projectId(), record.sourceUrl(), record.sourceDomain(), record.sourceTitle(),
                    timestamp(record.fetchTime()), record.rerankScore(), record.retrievalScore(), record.contentLength(), record.dedupeHash(), record.governanceStatus(),
                    record.promotionStatus(), record.promotionReason(), record.reviewerId(), record.reviewComment(), record.approvedDocumentId(),
                    timestamp(record.rolledBackAt()), record.contentText());
        }
    }

    public Optional<KbCandidateRecord> findById(String candidateId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from kb_candidate_governance where candidate_id = ?", mapper, candidateId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public PageResponse<KbCandidateRecord> list(Map<String, String> query) {
        int page = parseInt(query.get("page"), 1);
        int size = Math.min(parseInt(query.get("size"), 10), 100);
        int offset = (page - 1) * size;
        String status = blankToNull(query.get("promotionStatus"));
        String documentId = blankToNull(query.get("documentId"));
        String projectId = blankToNull(query.get("projectId"));
        String userId = blankToNull(query.get("userId"));
        String sourceDomain = blankToNull(query.get("sourceDomain"));
        StringBuilder where = new StringBuilder(" where 1 = 1 ");
        List<Object> args = new java.util.ArrayList<>();
        if (status != null) {
            where.append(" and promotion_status = ? ");
            args.add(status);
        }
        if (documentId != null) {
            where.append(" and document_id = ? ");
            args.add(documentId);
        }
        if (projectId != null) {
            where.append(" and project_id = ? ");
            args.add(projectId);
        }
        if (userId != null) {
            where.append(" and tenant_user_id = ? ");
            args.add(userId);
        }
        if (sourceDomain != null) {
            where.append(" and source_domain = ? ");
            args.add(sourceDomain);
        }
        Long total = jdbcTemplate.queryForObject("select count(1) from kb_candidate_governance" + where, Long.class, args.toArray());
        List<KbCandidateRecord> items = jdbcTemplate.query("select * from kb_candidate_governance" + where + " order by fetch_time desc limit ? offset ?",
                mapper, append(args, size, offset));
        return PageResponse.of(page, size, total == null ? 0 : total, items);
    }

    public long countByPromotionStatus(String status) {
        Long value = jdbcTemplate.queryForObject("select count(1) from kb_candidate_governance where promotion_status = ?", Long.class, status);
        return value == null ? 0L : value;
    }

    public List<KbCandidateRecord> recent(int limit) {
        return jdbcTemplate.query("select * from kb_candidate_governance order by fetch_time desc limit ?", mapper, limit);
    }

    public long count() {
        Long value = jdbcTemplate.queryForObject("select count(1) from kb_candidate_governance", Long.class);
        return value == null ? 0L : value;
    }

    private void ensureSchema() {
        jdbcTemplate.execute("""
                create table if not exists kb_candidate_governance(
                    candidate_id varchar(128) primary key,
                    document_id varchar(64) not null default '',
                    tenant_user_id varchar(64) not null default '',
                    project_id varchar(64) not null default '',
                    source_url text not null,
                    source_domain varchar(255) not null default '',
                    source_title varchar(255) not null default '',
                    fetch_time datetime not null default current_timestamp,
                    rerank_score double not null default 0,
                    retrieval_score double not null default 0,
                    content_length int not null default 0,
                    dedupe_hash varchar(128) not null default '',
                    governance_status varchar(64) not null default 'CANDIDATE_PENDING',
                    promotion_status varchar(64) not null default 'CANDIDATE_PENDING',
                    promotion_reason text null,
                    reviewer_id varchar(64) not null default '',
                    review_comment text null,
                    approved_document_id varchar(64) not null default '',
                    rolled_back_at datetime null,
                    content_text longtext null,
                    created_at datetime not null default current_timestamp,
                    updated_at datetime not null default current_timestamp on update current_timestamp,
                    key idx_project_status(project_id, promotion_status),
                    key idx_fetch_time(fetch_time),
                    key idx_source_domain(source_domain)
                )
                """);
    }

    private Object[] append(List<Object> args, Object... tail) {
        List<Object> values = new java.util.ArrayList<>(args);
        values.addAll(List.of(tail));
        return values.toArray();
    }

    private Timestamp timestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
