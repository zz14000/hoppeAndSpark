package com.hopeandsparks.task.service;

import com.hopeandsparks.task.dto.CreateAsyncTaskCommand;
import com.hopeandsparks.task.dto.RecordAsyncTaskEventCommand;
import com.hopeandsparks.task.vo.AsyncTaskVO;
import com.hopeandsparks.task.vo.AsyncTaskEventVO;

import java.util.List;
import java.util.Optional;

/**
 * 异步任务状态服务。
 * 业务模块只关心任务创建和进度更新，真正的 Redis Stream 投递仍然放在 arch-infra。
 */
public interface AsyncTaskService {

    AsyncTaskVO create(CreateAsyncTaskCommand command);

    AsyncTaskVO enqueue(String taskId, String message);

    Optional<AsyncTaskVO> findByTaskId(String taskId);

    AsyncTaskVO getByTaskId(String taskId);

    AsyncTaskVO start(String taskId);

    AsyncTaskVO updateProgress(String taskId, int progress, String message);

    AsyncTaskVO recordExternalRunId(String taskId, String externalRunId);

    AsyncTaskVO increaseRetry(String taskId);

    AsyncTaskVO markRetryWaiting(String taskId, String message);

    AsyncTaskVO markRolledBack(String taskId, String message);

    AsyncTaskVO markSuccess(String taskId, String message);

    AsyncTaskVO markFailed(String taskId, String failureReason);

    List<AsyncTaskVO> listByType(String taskType, int limit);

    AsyncTaskEventVO recordEvent(RecordAsyncTaskEventCommand command);

    List<AsyncTaskEventVO> listRecentEvents(String taskId, int limit);
}
