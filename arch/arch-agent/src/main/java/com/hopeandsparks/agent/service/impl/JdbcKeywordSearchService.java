package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.RetrievalHit;
import com.hopeandsparks.agent.service.KeywordSearchService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JdbcKeywordSearchService implements KeywordSearchService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcKeywordSearchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<RetrievalHit> searchFormal(String userId, String projectId, String query, int topK) {
        return jdbcTemplate.query("""
                select d.id as document_id,
                       c.id as chunk_id,
                       d.title,
                       x.source_url,
                       c.content_text,
                       c.chunk_index
                from kb_chunk_record c
                join kb_document d on d.id = c.document_id
                join kb_document_ext x on x.document_id = d.id
                where d.is_deleted = 0
                  and c.is_deleted = 0
                  and d.collection_name = 'edu_ground_truth'
                  and x.user_id = ?
                  and x.project_id = ?
                  and c.content_text like ?
                order by c.chunk_index asc
                limit ?
                """, (rs, rowNum) -> new RetrievalHit(
                "keyword",
                "edu_ground_truth",
                rs.getString("document_id"),
                rs.getString("chunk_id"),
                rs.getString("title"),
                rs.getString("source_url"),
                rs.getString("content_text"),
                score(rs.getString("content_text"), query),
                rowNum + 1,
                Map.of("chunkIndex", rs.getInt("chunk_index"))
        ), safe(userId), safe(projectId), "%" + safe(query) + "%", Math.max(1, topK));
    }

    @Override
    public List<RetrievalHit> searchCandidates(String userId, String projectId, String query, int topK) {
        return jdbcTemplate.query("""
                select candidate_id,
                       document_id,
                       source_title,
                       source_url,
                       content_text
                from kb_candidate_governance
                where tenant_user_id = ?
                  and project_id = ?
                  and content_text is not null
                  and content_text like ?
                order by fetch_time desc
                limit ?
                """, (rs, rowNum) -> new RetrievalHit(
                "candidate_keyword",
                "web_cache_candidates",
                rs.getString("document_id"),
                rs.getString("candidate_id"),
                rs.getString("source_title"),
                rs.getString("source_url"),
                rs.getString("content_text"),
                score(rs.getString("content_text"), query),
                rowNum + 1,
                Map.of("candidateId", rs.getString("candidate_id"))
        ), safe(userId), safe(projectId), "%" + safe(query) + "%", Math.max(1, topK));
    }

    private double score(String text, String query) {
        if (text == null || text.isBlank() || query == null || query.isBlank()) {
            return 0D;
        }
        int hit = 0;
        for (String token : query.split("\\s+")) {
            if (!token.isBlank() && text.contains(token)) {
                hit++;
            }
        }
        return Math.min(1D, 0.2D + hit * 0.15D);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
