package com.hopeandsparks.infra.file;

/**
 * 文件存储服务接口，是业务模块访问 MinIO 和 {@code sys_oss_file} 的统一入口。
 *
 * <p>后续知识库文档、资源导出、社区图片、头像等文件能力都应该通过这个接口完成，
 * 业务模块不要直接依赖 MinIO SDK，也不要自己拼 bucket 和 object key。</p>
 */
public interface FileStorageService {
}
