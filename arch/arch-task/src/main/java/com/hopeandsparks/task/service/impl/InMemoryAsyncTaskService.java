package com.hopeandsparks.task.service.impl;

import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.task.dto.CreateAsyncTaskCommand;
import com.hopeandsparks.task.entity.AsyncTask;
import com.hopeandsparks.task.enums.AsyncTaskStatus;
import com.hopeandsparks.task.service.AsyncTaskService;
import com.hopeandsparks.task.vo.AsyncTaskVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * W1 阶段的内存版任务服务。
 * 它不连接数据库，方便先把业务流程跑通；后续再替换成 MySQL + Mapper 实现。
 */
@Service
public class InMemoryAsyncTaskService implements AsyncTaskService {

    private final Map<String, AsyncTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, String> idempotencyIndex = new ConcurrentHashMap<>();

    @Override
    public synchronized AsyncTaskVO create(CreateAsyncTaskCommand command) {
        if (command == null || isBlank(command.taskType())) {
            throw new BusinessException(400, "taskType 不能为空");
        }
        if (!isBlank(command.idempotencyKey()) && idempotencyIndex.containsKey(command.idempotencyKey())) {
            return toVO(tasks.get(idempotencyIndex.get(command.idempotencyKey())));
        }

        LocalDateTime now = LocalDateTime.now();
        AsyncTask task = new AsyncTask();
        task.setTaskId("task_" + UUID.randomUUID().toString().replace("-", ""));
        task.setTaskType(command.taskType());
        task.setOwnerType(command.ownerType());
        task.setOwnerId(command.ownerId());
        task.setIdempotencyKey(command.idempotencyKey());
        task.setStatus(AsyncTaskStatus.PENDING);
        task.setProgress(0);
        task.setMessage("任务已创建");
        task.setRetryCount(0);
        task.setMaxRetry(command.maxRetry() == null ? 3 : Math.max(command.maxRetry(), 0));
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        tasks.put(task.getTaskId(), task);
        if (!isBlank(task.getIdempotencyKey())) {
            idempotencyIndex.put(task.getIdempotencyKey(), task.getTaskId());
        }
        return toVO(task);
    }

    @Override
    public Optional<AsyncTaskVO> findByTaskId(String taskId) {
        return Optional.ofNullable(tasks.get(taskId)).map(this::toVO);
    }

    @Override
    public AsyncTaskVO getByTaskId(String taskId) {
        return toVO(findInternal(taskId));
    }

    @Override
    public synchronized AsyncTaskVO start(String taskId) {
        AsyncTask task = findInternal(taskId);
        LocalDateTime now = LocalDateTime.now();
        task.setStatus(AsyncTaskStatus.PROCESSING);
        task.setProgress(Math.max(task.getProgress(), 1));
        task.setStartedAt(task.getStartedAt() == null ? now : task.getStartedAt());
        task.setUpdatedAt(now);
        task.setMessage("任务处理中");
        return toVO(task);
    }

    @Override
    public synchronized AsyncTaskVO updateProgress(String taskId, int progress, String message) {
        AsyncTask task = findInternal(taskId);
        task.setProgress(clampProgress(progress));
        task.setMessage(message);
        task.setUpdatedAt(LocalDateTime.now());
        return toVO(task);
    }

    @Override
    public synchronized AsyncTaskVO recordExternalRunId(String taskId, String externalRunId) {
        AsyncTask task = findInternal(taskId);
        task.setExternalRunId(externalRunId);
        task.setUpdatedAt(LocalDateTime.now());
        return toVO(task);
    }

    @Override
    public synchronized AsyncTaskVO increaseRetry(String taskId) {
        AsyncTask task = findInternal(taskId);
        task.setRetryCount(task.getRetryCount() + 1);
        task.setUpdatedAt(LocalDateTime.now());
        return toVO(task);
    }

    @Override
    public synchronized AsyncTaskVO markSuccess(String taskId, String message) {
        AsyncTask task = findInternal(taskId);
        LocalDateTime now = LocalDateTime.now();
        task.setStatus(AsyncTaskStatus.SUCCESS);
        task.setProgress(100);
        task.setMessage(isBlank(message) ? "任务已完成" : message);
        task.setFinishedAt(now);
        task.setUpdatedAt(now);
        return toVO(task);
    }

    @Override
    public synchronized AsyncTaskVO markFailed(String taskId, String failureReason) {
        AsyncTask task = findInternal(taskId);
        LocalDateTime now = LocalDateTime.now();
        task.setStatus(AsyncTaskStatus.FAILED);
        task.setFailureReason(failureReason);
        task.setMessage("任务失败");
        task.setFinishedAt(now);
        task.setUpdatedAt(now);
        return toVO(task);
    }

    private AsyncTask findInternal(String taskId) {
        AsyncTask task = tasks.get(taskId);
        if (task == null) {
            throw new BusinessException(404, "异步任务不存在");
        }
        return task;
    }

    private AsyncTaskVO toVO(AsyncTask task) {
        return new AsyncTaskVO(
                task.getTaskId(),
                task.getTaskType(),
                task.getOwnerType(),
                task.getOwnerId(),
                task.getStatus(),
                task.getProgress(),
                task.getMessage(),
                task.getFailureReason(),
                task.getExternalRunId(),
                task.getRetryCount(),
                task.getMaxRetry(),
                task.getCreatedAt(),
                task.getStartedAt(),
                task.getFinishedAt(),
                task.getUpdatedAt()
        );
    }

    private int clampProgress(int progress) {
        return Math.max(0, Math.min(progress, 100));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
