package com.hopeandsparks.task.service;

/**
 * 异步任务状态服务边界，负责创建和维护 {@code async_generation_task}。
 *
 * <p>业务模块需要发起异步任务时，先调用这里创建任务并拿到 taskId，再调用 infra 的
 * Redis Stream 客户端投递消息。consumer 执行开始、成功、失败、重试时也通过这里更新状态，
 * 让前台和后台都能按 taskId 查询进度。</p>
 */
public interface AsyncTaskService {
}
