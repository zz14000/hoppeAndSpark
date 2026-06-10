package com.hopeandsparks.kb.repository;

import com.hopeandsparks.common.response.PageResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class KbDocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<KbDocumentRecord> documentMapper = (rs, rowNum) -> new KbDocumentRecord(
            rs.getLong("id"),
            rs.getString("kb_domain"),
            rs.getString("project_id"),
            rs.getString("title"),
            rs.getString("file_id_text"),
            rs.getString("doc_type"),
            rs.getString("source_type"),
            rs.getString("source_url"),
            rs.getString("content_text"),
            rs.getString("collection_name"),
            rs.getString("embedding_model"),
            rs.getString("embedding_version"),
            rs.getInt("document_version"),
            rs.getInt("total_tokens"),
            rs.getInt("chunk_count"),
            rs.getString("parse_status"),
            rs.getString("error_msg"),
            rs.getString("user_id"),
            rs.getInt("is_deleted") == 1
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
            rs.getString("section_path")
    );

    public KbDocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureSchema();
    }

    public PageResponse<KbDocumentRecord> listDocuments(Map<String, String> query) {
        int page = parseInt(query.get("page"), 1);
        int size = Math.min(parseInt(query.get("size"), 10), 100);
        int offset = (page - 1) * size;
        String keyword = blankToNull(query.get("keyword"));
        String parseStatus = blankToNull(query.get("parseStatus"));
        List<Object> args = new java.util.ArrayList<>();
        StringBuilder where = new StringBuilder(" where d.is_deleted = 0 ");
        if (keyword != null) {
            where.append(" and (d.title like ? or x.source_url like ?) ");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        if (parseStatus != null) {
            where.append(" and d.parse_status = ? ");
            args.add(parseStatus);
        }
        Long total = jdbcTemplate.queryForObject("""
                select count(1)
                from kb_document d
                left join kb_document_ext x on x.document_id = d.id
                """ + where, Long.class, args.toArray());
        List<KbDocumentRecord> records = jdbcTemplate.query("""
                select d.id, d.kb_domain, x.project_id, d.title, x.file_id_text, d.doc_type, d.source_type,
                       x.source_url, x.content_text, d.collection_name, d.embedding_model, d.embedding_version,
                       d.document_version, d.total_tokens, d.chunk_count, d.parse_status, d.error_msg, x.user_id, d.is_deleted
                from kb_document d
                left join kb_document_ext x on x.document_id = d.id
                """ + where + " order by d.id desc limit ? offset ?", documentMapper, append(args, size, offset));
        return PageResponse.of(page, size, total == null ? 0 : total, records);
    }

    public Optional<KbDocumentRecord> findDocument(Long documentId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select d.id, d.kb_domain, x.project_id, d.title, x.file_id_text, d.doc_type, d.source_type,
                           x.source_url, x.content_text, d.collection_name, d.embedding_model, d.embedding_version,
                           d.document_version, d.total_tokens, d.chunk_count, d.parse_status, d.error_msg, x.user_id, d.is_deleted
                    from kb_document d
                    left join kb_document_ext x on x.document_id = d.id
                    where d.id = ? and d.is_deleted = 0
                    """, documentMapper, documentId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Long insertDocument(KbDocumentRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into kb_document(
                        kb_domain, course_id, node_id, title, file_id, doc_type, source_type, collection_name,
                        parse_strategy_id, embedding_model, embedding_version, document_version, total_tokens,
                        chunk_count, parse_status, uploader_id, error_msg, is_deleted
                    ) values (?, null, null, ?, ?, ?, ?, ?, null, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, record.domain());
            ps.setString(2, record.title());
            ps.setLong(3, parseLong(record.fileId(), 0L));
            ps.setString(4, record.docType());
            ps.setString(5, record.sourceType());
            ps.setString(6, record.collectionName());
            ps.setString(7, record.embeddingModel());
            ps.setString(8, record.embeddingVersion());
            ps.setInt(9, record.documentVersion());
            ps.setInt(10, record.totalTokens());
            ps.setInt(11, record.chunkCount());
            ps.setString(12, record.parseStatus());
            ps.setLong(13, parseLong(record.userId(), 0L));
            ps.setString(14, record.errorMessage());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        Long documentId = key == null ? null : key.longValue();
        if (documentId == null) {
            throw new IllegalStateException("Failed to generate kb_document id");
        }
        upsertExt(documentId, record);
        return documentId;
    }

    public void updateDocument(Long documentId, KbDocumentRecord record) {
        jdbcTemplate.update("""
                update kb_document
                set kb_domain = ?, title = ?, doc_type = ?, source_type = ?, collection_name = ?,
                    document_version = ?, parse_status = ?, error_msg = ?
                where id = ? and is_deleted = 0
                """,
                record.domain(), record.title(), record.docType(), record.sourceType(), record.collectionName(),
                record.documentVersion(), record.parseStatus(), record.errorMessage(), documentId);
        upsertExt(documentId, record);
    }

    public void updateParseResult(Long documentId, String parseStatus, int totalTokens, int chunkCount, String errorMessage) {
        jdbcTemplate.update("""
                update kb_document
                set parse_status = ?, total_tokens = ?, chunk_count = ?, error_msg = ?
                where id = ?
                """, parseStatus, totalTokens, chunkCount, errorMessage, documentId);
    }

    public void markDeleted(Long documentId) {
        jdbcTemplate.update("update kb_document set is_deleted = 1 where id = ?", documentId);
        jdbcTemplate.update("update kb_chunk_record set is_deleted = 1, is_active = 0 where document_id = ?", documentId);
    }

    public List<KbDocumentRecord> loadPendingDocuments(int limit) {
        return jdbcTemplate.query("""
                select d.id, d.kb_domain, x.project_id, d.title, x.file_id_text, d.doc_type, d.source_type,
                       x.source_url, x.content_text, d.collection_name, d.embedding_model, d.embedding_version,
                       d.document_version, d.total_tokens, d.chunk_count, d.parse_status, d.error_msg, x.user_id, d.is_deleted
                from kb_document d
                left join kb_document_ext x on x.document_id = d.id
                where d.is_deleted = 0 and d.parse_status in ('pending', 'failed')
                order by d.id asc
                limit ?
                """, documentMapper, limit);
    }

    public void replaceChunks(Long documentId, List<KbChunkRecord> chunks) {
        jdbcTemplate.update("delete from kb_chunk_record where document_id = ?", documentId);
        for (KbChunkRecord chunk : chunks) {
            jdbcTemplate.update("""
                    insert into kb_chunk_record(document_id, chunk_index, content_text, token_size, chroma_point_id, embed_status, is_active, is_deleted, section_path)
                    values (?, ?, ?, ?, ?, ?, ?, 0, ?)
                    """,
                    documentId, chunk.chunkIndex(), chunk.contentText(), chunk.tokenSize(), chunk.chromaPointId(),
                    chunk.embedStatus(), chunk.active() ? 1 : 0, chunk.sectionPath());
        }
    }

    public List<KbChunkRecord> listChunks(Long documentId) {
        return jdbcTemplate.query("""
                select id, document_id, chunk_index, content_text, token_size, chroma_point_id, embed_status, is_active, section_path
                from kb_chunk_record
                where document_id = ? and is_deleted = 0
                order by chunk_index asc
                """, chunkMapper, documentId);
    }

    public boolean existsActiveSourceUrl(String userId, String projectId, String collectionName, String sourceUrl) {
        Long count = jdbcTemplate.queryForObject("""
                select count(1)
                from kb_document d
                left join kb_document_ext x on x.document_id = d.id
                where d.is_deleted = 0
                  and d.collection_name = ?
                  and x.user_id = ?
                  and x.project_id = ?
                  and x.source_url = ?
                """, Long.class, collectionName, safe(userId), safe(projectId), safe(sourceUrl));
        return count != null && count > 0;
    }

    public long countDocuments() {
        Long count = jdbcTemplate.queryForObject("select count(1) from kb_document where is_deleted = 0", Long.class);
        return count == null ? 0L : count;
    }

    public double averageChunkCount() {
        Double value = jdbcTemplate.queryForObject("select coalesce(avg(chunk_count), 0) from kb_document where is_deleted = 0", Double.class);
        return value == null ? 0D : value;
    }

    public double averageChunkLength() {
        Double value = jdbcTemplate.queryForObject("""
                select coalesce(avg(char_length(content_text)), 0)
                from kb_chunk_record
                where is_deleted = 0
                """, Double.class);
        return value == null ? 0D : value;
    }

    public void markRolledBack(Long documentId, String errorMessage) {
        jdbcTemplate.update("""
                update kb_document
                set parse_status = 'rolled_back', error_msg = ?
                where id = ?
                """, errorMessage, documentId);
    }

    public void updateChunkContent(Long chunkId, String contentText) {
        jdbcTemplate.update("""
                update kb_chunk_record
                set content_text = ?, embed_status = 0
                where id = ? and is_deleted = 0
                """, contentText, chunkId);
    }

    private void upsertExt(Long documentId, KbDocumentRecord record) {
        Integer count = jdbcTemplate.queryForObject("select count(1) from kb_document_ext where document_id = ?", Integer.class, documentId);
        if (count != null && count > 0) {
            jdbcTemplate.update("""
                    update kb_document_ext
                    set project_id = ?, user_id = ?, file_id_text = ?, source_url = ?, content_text = ?
                    where document_id = ?
                    """, record.projectId(), record.userId(), record.fileId(), record.sourceUrl(), record.contentText(), documentId);
        } else {
            jdbcTemplate.update("""
                    insert into kb_document_ext(document_id, project_id, user_id, file_id_text, source_url, content_text)
                    values (?, ?, ?, ?, ?, ?)
                    """, documentId, record.projectId(), record.userId(), record.fileId(), record.sourceUrl(), record.contentText());
        }
    }

    private Object[] append(List<Object> args, Object... tail) {
        List<Object> values = new java.util.ArrayList<>(args);
        values.addAll(List.of(tail));
        return values.toArray();
    }

    private void ensureSchema() {
        jdbcTemplate.execute("""
                create table if not exists kb_document(
                    id bigint primary key auto_increment,
                    kb_domain varchar(100) not null,
                    course_id bigint null,
                    node_id bigint null,
                    title varchar(255) not null,
                    file_id bigint not null default 0,
                    doc_type varchar(50) not null,
                    source_type varchar(20) not null default 'official',
                    collection_name varchar(100) not null,
                    parse_strategy_id bigint null,
                    embedding_model varchar(100) not null,
                    embedding_version varchar(50) null,
                    document_version int not null default 1,
                    total_tokens int not null default 0,
                    chunk_count int not null default 0,
                    parse_status varchar(20) not null default 'pending',
                    uploader_id bigint not null default 0,
                    error_msg text null,
                    created_at datetime not null default current_timestamp,
                    updated_at datetime not null default current_timestamp on update current_timestamp,
                    is_deleted tinyint not null default 0
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists kb_chunk_record(
                    id bigint primary key auto_increment,
                    document_id bigint not null,
                    chunk_index int not null,
                    content_text text not null,
                    token_size int not null default 0,
                    chroma_point_id varchar(128) null,
                    embed_status tinyint not null default 0,
                    is_active tinyint not null default 1,
                    created_at datetime not null default current_timestamp,
                    updated_at datetime not null default current_timestamp on update current_timestamp,
                    is_deleted tinyint not null default 0,
                    section_path varchar(255) null,
                    unique key uk_document_chunk_index(document_id, chunk_index),
                    unique key uk_chroma_point_id(chroma_point_id)
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists kb_document_ext(
                    document_id bigint primary key,
                    project_id varchar(64) null,
                    user_id varchar(64) null,
                    file_id_text varchar(128) null,
                    source_url text null,
                    content_text longtext null,
                    created_at datetime not null default current_timestamp,
                    updated_at datetime not null default current_timestamp on update current_timestamp
                )
                """);
        ensureColumn("kb_chunk_record", "section_path", "alter table kb_chunk_record add column section_path varchar(255) null");
    }

    private void ensureColumn(String tableName, String columnName, String ddl) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(1)
                from information_schema.columns
                where table_schema = database() and table_name = ? and column_name = ?
                """, Integer.class, tableName, columnName);
        if (count == null || count == 0) {
            jdbcTemplate.execute(ddl);
        }
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

    private long parseLong(String value, long defaultValue) {
        try {
            return value == null || value.isBlank() ? defaultValue : Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
