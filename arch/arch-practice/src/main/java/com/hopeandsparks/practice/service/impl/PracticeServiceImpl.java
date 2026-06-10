package com.hopeandsparks.practice.service.impl;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.practice.dto.CoachHintRequest;
import com.hopeandsparks.practice.dto.CodeRunRequest;
import com.hopeandsparks.practice.dto.PracticeAnswerRequest;
import com.hopeandsparks.practice.dto.PracticeSubmitRequest;
import com.hopeandsparks.practice.dto.QuestionFlagRequest;
import com.hopeandsparks.practice.service.PracticeService;
import com.hopeandsparks.practice.vo.AnswerSaveVO;
import com.hopeandsparks.practice.vo.CoachHintVO;
import com.hopeandsparks.practice.vo.CodeRunVO;
import com.hopeandsparks.practice.vo.EvaluationReportVO;
import com.hopeandsparks.practice.vo.ExerciseSetCardVO;
import com.hopeandsparks.practice.vo.ExerciseSetDetailVO;
import com.hopeandsparks.practice.vo.FlagVO;
import com.hopeandsparks.practice.vo.PracticeSubmitVO;
import com.hopeandsparks.practice.vo.QuestionAnswerDetailVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PracticeServiceImpl implements PracticeService {

    @Override
    public PageResponse<ExerciseSetCardVO> listSets(AuthenticatedPrincipal principal, Map<String, String> query) {
        return PageResponse.of(1, 10, 1, List.of(new ExerciseSetCardVO("mock-set", "Mock Exercise Set", "medium", true)));
    }

    @Override
    public ExerciseSetDetailVO detail(AuthenticatedPrincipal principal, String exerciseSetId) {
        return new ExerciseSetDetailVO(exerciseSetId, "Mock Exercise Set", List.of("q1"), true);
    }

    @Override
    public QuestionAnswerDetailVO question(AuthenticatedPrincipal principal, String exerciseSetId, String questionId) {
        return new QuestionAnswerDetailVO(questionId, "Mock question text", "", true);
    }

    @Override
    public AnswerSaveVO saveAnswer(AuthenticatedPrincipal principal, String exerciseSetId, String questionId, PracticeAnswerRequest request) {
        return new AnswerSaveVO(questionId, true, true);
    }

    @Override
    public FlagVO flagQuestion(AuthenticatedPrincipal principal, String exerciseSetId, String questionId, QuestionFlagRequest request) {
        return new FlagVO(questionId, request != null && Boolean.TRUE.equals(request.flagged()), true);
    }

    @Override
    public CoachHintVO hint(AuthenticatedPrincipal principal, String exerciseSetId, String questionId, CoachHintRequest request) {
        int level = request == null || request.hintLevel() == null ? 1 : request.hintLevel();
        return new CoachHintVO(questionId, "Mock Coach hint level " + level, level, true);
    }

    @Override
    public PracticeSubmitVO submit(AuthenticatedPrincipal principal, String exerciseSetId, PracticeSubmitRequest request) {
        return new PracticeSubmitVO(true, request == null ? "mock-attempt" : request.attemptId(), "submitted", true);
    }

    @Override
    public CodeRunVO codeRun(AuthenticatedPrincipal principal, String exerciseSetId, String questionId, CodeRunRequest request) {
        return new CodeRunVO(questionId, "AI_REVIEW_ONLY", "mock code review passed", true);
    }

    @Override
    public EvaluationReportVO report(AuthenticatedPrincipal principal, String attemptId) {
        return new EvaluationReportVO(attemptId, "mock evaluation report", List.of(), true);
    }
}
