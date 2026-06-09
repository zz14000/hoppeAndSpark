package com.hopeandsparks.task.service;

import com.hopeandsparks.task.dto.CreateAsyncTaskCommand;
import com.hopeandsparks.task.vo.AsyncTaskVO;

import java.util.Optional;

/**
 * 异步任务状态服务。
 * 业务模块只关心任务创建和进度更新，真正的 Redis Stream 投递仍然放在 arch-infra。
 */
public interface AsyncTaskService {

    AsyncTaskVO create(CreateAsyncTaskCommand command);

    Optional<AsyncTaskVO> findByTaskId(String taskId);

    AsyncTaskVO getByTaskId(String taskId);

    AsyncTaskVO start(String taskId);

    AsyncTaskVO updateProgress(String taskId, int progress, String message);

    AsyncTaskVO recordExternalRunId(String taskId, String externalRunId);

    AsyncTaskVO increaseRetry(String taskId);

    AsyncTaskVO markSuccess(String taskId, String message);

    AsyncTaskVO markFailed(String taskId, String failureReason);
}
