package com.hopeandsparks.kb.repository;

import com.hopeandsparks.kb.entity.KbChunkRecord;
import com.hopeandsparks.kb.entity.KbDocument;
import com.hopeandsparks.kb.entity.KbParseStrategy;
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
import java.util.Map;
import java.util.Optional;

/**
 * JdbcTemplate repository for KB document and chunk tables.
 */
@Repository
public class KbDocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<KbDocument> documentMapper = (rs, rowNum) -> new KbDocument(
            rs.getLong("id"),
            rs.getString("kb_domain"),
            rs.getObject("course_id", Long.class),
            rs.getObject("node_id", Long.class),
            rs.getString("title"),
            rs.getLong("file_id"),
            rs.getString("file_name"),
            rs.getString("file_type"),
            rs.getObject("file_size", Long.class),
            rs.getString("doc_type"),
            rs.getString("source_type"),
            rs.getString("collection_name"),
            rs.getObject("parse_strategy_id", Long.class),
            rs.getString("embedding_model"),
            rs.getString("embedding_version"),
            rs.getInt("document_version"),
            rs.getInt("total_tokens"),
            rs.getInt("chunk_count"),
            rs.getString("parse_status"),
            rs.getLong("uploader_id"),
            rs.getString("error_msg"),
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
    );

    private final RowMapper<KbChunkRecord> chunkMapper = (rs, rowNum) -> new KbChunkRecord(
            rs.getLong("id"),
            rs.getLong("document_id"),
            rs.getInt("chunk_index"),
            rs.getString("content_text"),
            rs.getInt("token_size"),
            rs.getString("chroma_point_id"),
            rs.getInt("embed_status"),
            rs.getInt("is_active") == 1,
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
    );

    private final RowMapper<KbParseStrategy> strategyMapper = (rs, rowNum) -> new KbParseStrategy(
            rs.getLong("id"),
            rs.getString("strategy_name"),
            rs.getInt("chunk_size"),
            rs.getInt("chunk_overlap")
    );

    public KbDocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countDocuments(Map<String, String> query) {
        List<Object> args = new ArrayList<>();
        Long total = jdbcTemplate.queryForObject("""
                select count(1)
                from kb_document d
                left join sys_oss_file f on f.id = d.file_id and f.is_deleted = 0
                where d.is_deleted = 0
                """ + documentFilters(query, args), Long.class, args.toArray());
        return total == null ? 0 : total;
    }

    public List<KbDocument> listDocuments(Map<String, String> query, long offset, long pageSize) {
        List<Object> args = new ArrayList<>();
        String sql = baseDocumentSelect() + documentFilters(query, args) + """
                order by d.updated_at desc, d.id desc
                limit ? offset ?
                """;
        args.add(pageSize);
        args.add(offset);
        return jdbcTemplate.query(sql, documentMapper, args.toArray());
    }

    public Optional<KbDocument> findById(Long documentId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(baseDocumentSelect() + """
                    and d.id = ?
                    """, documentMapper, documentId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Long insertDocument(
            String kbDomain,
            Long courseId,
            Long nodeId,
            String title,
            Long fileId,
            String docType,
            String sourceType,
            String collectionName,
            Long parseStrategyId,
            String embeddingModel,
            String embeddingVersion,
            Long uploaderId
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into kb_document(kb_domain, course_id, node_id, title, file_id,
                                            doc_type, source_type, collection_name,
                                            parse_strategy_id, embedding_model,
                                            embedding_version, parse_status, uploader_id)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending', ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, kbDomain);
            setLong(ps, 2, courseId);
            setLong(ps, 3, nodeId);
            ps.setString(4, title);
            ps.setLong(5, fileId);
            ps.setString(6, docType);
            ps.setString(7, sourceType);
            ps.setString(8, collectionName);
            setLong(ps, 9, parseStrategyId);
            ps.setString(10, embeddingModel);
            ps.setString(11, embeddingVersion);
            ps.setLong(12, uploaderId);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public void updateDocument(
            Long documentId,
            String kbDomain,
            Long courseId,
            Long nodeId,
            String title,
            String docType,
            String sourceType,
            String collectionName,
            Long parseStrategyId,
            String embeddingModel,
            String embeddingVersion
    ) {
        jdbcTemplate.update("""
                update kb_document
                set kb_domain = ?,
                    course_id = ?,
                    node_id = ?,
                    title = ?,
                    doc_type = ?,
                    source_type = ?,
                    collection_name = ?,
                    parse_strategy_id = ?,
                    embedding_model = ?,
                    embedding_version = ?
                where id = ? and is_deleted = 0
                """, kbDomain, courseId, nodeId, title, docType, sourceType, collectionName,
                parseStrategyId, embeddingModel, embeddingVersion, documentId);
    }

    public void markDocumentDeleted(Long documentId) {
        jdbcTemplate.update("""
                update kb_document
                set is_deleted = 1,
                    parse_status = 'failed',
                    error_msg = 'document deleted by manage'
                where id = ? and is_deleted = 0
                """, documentId);
        jdbcTemplate.update("""
                update kb_chunk_record
                set is_deleted = 1,
                    is_active = 0
                where document_id = ? and is_deleted = 0
                """, documentId);
    }

    public void resetForParse(Long documentId) {
        jdbcTemplate.update("delete from kb_chunk_record where document_id = ?", documentId);
        jdbcTemplate.update("""
                update kb_document
                set document_version = document_version + 1,
                    total_tokens = 0,
                    chunk_count = 0,
                    parse_status = 'pending',
                    error_msg = null
                where id = ? and is_deleted = 0
                """, documentId);
    }

    public void updateParseStatus(Long documentId, String status, String errorMsg) {
        jdbcTemplate.update("""
                update kb_document
                set parse_status = ?,
                    error_msg = ?
                where id = ? and is_deleted = 0
                """, status, errorMsg, documentId);
    }

    public void updateParseResult(Long documentId, int totalTokens, int chunkCount, String status, String errorMsg) {
        jdbcTemplate.update("""
                update kb_document
                set total_tokens = ?,
                    chunk_count = ?,
                    parse_status = ?,
                    error_msg = ?
                where id = ? and is_deleted = 0
                """, totalTokens, chunkCount, status, errorMsg, documentId);
    }

    public void insertChunk(Long documentId, int chunkIndex, String contentText, int tokenSize) {
        jdbcTemplate.update("""
                insert into kb_chunk_record(document_id, chunk_index, content_text, token_size, embed_status, is_active)
                values (?, ?, ?, ?, 0, 1)
                """, documentId, chunkIndex, contentText, tokenSize);
    }

    public void markChunkEmbedded(Long documentId, int chunkIndex, String chromaPointId) {
        jdbcTemplate.update("""
                update kb_chunk_record
                set embed_status = 1,
                    chroma_point_id = ?
                where document_id = ? and chunk_index = ? and is_deleted = 0
                """, chromaPointId, documentId, chunkIndex);
    }

    public long countChunks(Long documentId) {
        Long total = jdbcTemplate.queryForObject("""
                select count(1)
                from kb_chunk_record
                where document_id = ? and is_deleted = 0
                """, Long.class, documentId);
        return total == null ? 0 : total;
    }

    public List<KbChunkRecord> listChunks(Long documentId, long offset, long pageSize) {
        return jdbcTemplate.query("""
                select id, document_id, chunk_index, content_text, token_size,
                       chroma_point_id, embed_status, is_active, created_at, updated_at
                from kb_chunk_record
                where document_id = ? and is_deleted = 0
                order by chunk_index asc
                limit ? offset ?
                """, chunkMapper, documentId, pageSize, offset);
    }

    public Optional<KbChunkRecord> findChunkById(Long chunkId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, document_id, chunk_index, content_text, token_size,
                           chroma_point_id, embed_status, is_active, created_at, updated_at
                    from kb_chunk_record
                    where id = ? and is_deleted = 0
                    """, chunkMapper, chunkId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public void updateChunkContent(Long chunkId, String contentText, int tokenSize, String chromaPointId) {
        jdbcTemplate.update("""
                update kb_chunk_record
                set content_text = ?,
                    token_size = ?,
                    embed_status = 1,
                    chroma_point_id = ?
                where id = ? and is_deleted = 0
                """, contentText, tokenSize, chromaPointId, chunkId);
    }

    public void refreshDocumentChunkStats(Long documentId) {
        jdbcTemplate.update("""
                update kb_document d
                set d.chunk_count = (
                        select count(1)
                        from kb_chunk_record c
                        where c.document_id = d.id and c.is_deleted = 0
                    ),
                    d.total_tokens = (
                        select coalesce(sum(c.token_size), 0)
                        from kb_chunk_record c
                        where c.document_id = d.id and c.is_deleted = 0
                    ),
                    d.parse_status = 'success',
                    d.error_msg = null
                where d.id = ? and d.is_deleted = 0
                """, documentId);
    }

    public Optional<KbParseStrategy> findStrategy(Long strategyId) {
        if (strategyId == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, strategy_name, chunk_size, chunk_overlap
                    from kb_parse_strategy
                    where id = ? and status = 1 and is_deleted = 0
                    """, strategyMapper, strategyId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public boolean existsFile(Long fileId) {
        return exists("""
                select count(1)
                from sys_oss_file
                where id = ? and is_deleted = 0
                """, fileId);
    }

    public boolean existsCourse(Long courseId) {
        return exists("""
                select count(1)
                from course
                where id = ? and is_deleted = 0
                """, courseId);
    }

    public boolean existsNode(Long nodeId) {
        return exists("""
                select count(1)
                from knowledge_node
                where id = ? and is_deleted = 0
                """, nodeId);
    }

    private boolean exists(String sql, Long id) {
        if (id == null) {
            return true;
        }
        Long total = jdbcTemplate.queryForObject(sql, Long.class, id);
        return total != null && total > 0;
    }

    private String baseDocumentSelect() {
        return """
                select d.id, d.kb_domain, d.course_id, d.node_id, d.title,
                       d.file_id, f.file_name, f.file_type, f.file_size,
                       d.doc_type, d.source_type, d.collection_name,
                       d.parse_strategy_id, d.embedding_model, d.embedding_version,
                       d.document_version, d.total_tokens, d.chunk_count,
                       d.parse_status, d.uploader_id, d.error_msg,
                       d.created_at, d.updated_at
                from kb_document d
                left join sys_oss_file f on f.id = d.file_id and f.is_deleted = 0
                where d.is_deleted = 0
                """;
    }

    private String documentFilters(Map<String, String> query, List<Object> args) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        if (!isBlank(query.get("status"))) {
            sql.append(" and d.parse_status = ?");
            args.add(query.get("status").trim());
        }
        if (!isBlank(query.get("parseStatus"))) {
            sql.append(" and d.parse_status = ?");
            args.add(query.get("parseStatus").trim());
        }
        if (!isBlank(query.get("kbDomain"))) {
            sql.append(" and d.kb_domain = ?");
            args.add(query.get("kbDomain").trim());
        }
        addLongFilter(sql, args, "d.course_id", query.get("courseId"));
        addLongFilter(sql, args, "d.node_id", query.get("nodeId"));
        if (!isBlank(query.get("keyword"))) {
            String like = "%" + query.get("keyword").trim() + "%";
            sql.append(" and (d.title like ? or d.kb_domain like ? or f.file_name like ?)");
            args.add(like);
            args.add(like);
            args.add(like);
        }
        return sql.toString();
    }

    private void addLongFilter(StringBuilder sql, List<Object> args, String column, String value) {
        Long parsed = parseOptionalId(value);
        if (parsed != null) {
            sql.append(" and ").append(column).append(" = ?");
            args.add(parsed);
        }
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

    private void setLong(PreparedStatement ps, int index, Long value) throws java.sql.SQLException {
        if (value == null) {
            ps.setObject(index, null);
        } else {
            ps.setLong(index, value);
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
