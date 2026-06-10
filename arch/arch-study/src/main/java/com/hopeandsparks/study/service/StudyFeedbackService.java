package com.hopeandsparks.study.service;

import com.hopeandsparks.study.dto.PracticeWeakPointFeedback;

import java.util.List;

/**
 * 学习计划模块接收外部学习反馈的入口。
 */
public interface StudyFeedbackService {

    /**
     * 接收练习评测产生的薄弱点，并更新用户知识点进度。
     */
    void acceptPracticeWeakPoints(Long userId, List<PracticeWeakPointFeedback> weakPoints);
}
