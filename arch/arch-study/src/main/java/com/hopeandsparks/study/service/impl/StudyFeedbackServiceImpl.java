package com.hopeandsparks.study.service.impl;

import com.hopeandsparks.study.dto.PracticeWeakPointFeedback;
import com.hopeandsparks.study.repository.StudyRepository;
import com.hopeandsparks.study.service.StudyFeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 根据练习结果调整知识点掌握度。
 */
@Service
public class StudyFeedbackServiceImpl implements StudyFeedbackService {

    private final StudyRepository studyRepository;

    public StudyFeedbackServiceImpl(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    @Override
    @Transactional
    public void acceptPracticeWeakPoints(Long userId, List<PracticeWeakPointFeedback> weakPoints) {
        if (userId == null || weakPoints == null || weakPoints.isEmpty()) {
            return;
        }
        for (PracticeWeakPointFeedback feedback : weakPoints) {
            if (feedback == null || feedback.nodeId() == null) {
                continue;
            }
            BigDecimal accuracy = clampAccuracy(feedback.accuracy());
            String status = progressStatus(accuracy);
            int progress = progressPercent(accuracy);
            int nextReviewDays = nextReviewDays(accuracy);
            studyRepository.applyPracticeFeedback(
                    userId,
                    feedback.nodeId(),
                    status,
                    progress,
                    accuracy,
                    nextReviewDays
            );
        }
    }

    private BigDecimal clampAccuracy(BigDecimal accuracy) {
        if (accuracy == null) {
            return BigDecimal.ZERO;
        }
        if (accuracy.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal hundred = BigDecimal.valueOf(100);
        if (accuracy.compareTo(hundred) > 0) {
            return hundred;
        }
        return accuracy;
    }

    private String progressStatus(BigDecimal accuracy) {
        if (accuracy.compareTo(BigDecimal.valueOf(85)) >= 0) {
            return "mastered";
        }
        if (accuracy.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "learning";
        }
        return "review";
    }

    private int progressPercent(BigDecimal accuracy) {
        if (accuracy.compareTo(BigDecimal.valueOf(85)) >= 0) {
            return 100;
        }
        if (accuracy.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return 70;
        }
        return 40;
    }

    private int nextReviewDays(BigDecimal accuracy) {
        if (accuracy.compareTo(BigDecimal.valueOf(60)) < 0) {
            return 1;
        }
        if (accuracy.compareTo(BigDecimal.valueOf(85)) < 0) {
            return 3;
        }
        return 14;
    }
}
