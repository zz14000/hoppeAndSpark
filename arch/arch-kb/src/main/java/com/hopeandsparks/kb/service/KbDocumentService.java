package com.hopeandsparks.kb.service;

/**
 * 知识库文档业务服务边界，负责文档上传后的业务落库、解析、向量化和重解析。
 *
 * <p>Manage 后台的知识库 Controller 会调用这里完成真正业务动作；文件对象由
 * {@code arch-infra} 管理，任务状态由 {@code arch-task} 管理，kb 模块只负责
 * {@code kb_document} 和 {@code kb_chunk_record} 等知识库业务状态。</p>
 */
public interface KbDocumentService {
}
