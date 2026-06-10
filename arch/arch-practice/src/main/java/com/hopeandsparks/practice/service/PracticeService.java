package com.hopeandsparks.practice.service;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.practice.dto.CoachHintRequest;
import com.hopeandsparks.practice.dto.CodeRunRequest;
import com.hopeandsparks.practice.dto.PracticeAnswerRequest;
import com.hopeandsparks.practice.dto.PracticeSubmitRequest;
import com.hopeandsparks.practice.dto.QuestionFlagRequest;
import com.hopeandsparks.practice.vo.AnswerSaveVO;
import com.hopeandsparks.practice.vo.CoachHintVO;
import com.hopeandsparks.practice.vo.CodeRunVO;
import com.hopeandsparks.practice.vo.EvaluationReportVO;
import com.hopeandsparks.practice.vo.ExerciseSetCardVO;
import com.hopeandsparks.practice.vo.ExerciseSetDetailVO;
import com.hopeandsparks.practice.vo.FlagVO;
import com.hopeandsparks.practice.vo.PracticeSubmitVO;
import com.hopeandsparks.practice.vo.QuestionAnswerDetailVO;

import java.util.Map;

/**
 * 练习与评测用例服务。
 */
public interface PracticeService {

    PageResponse<ExerciseSetCardVO> listSets(AuthenticatedPrincipal principal, Map<String, String> query);

    ExerciseSetDetailVO detail(AuthenticatedPrincipal principal, String exerciseSetId);

    QuestionAnswerDetailVO question(AuthenticatedPrincipal principal, String exerciseSetId, String questionId);

    AnswerSaveVO saveAnswer(
            AuthenticatedPrincipal principal,
            String exerciseSetId,
            String questionId,
            PracticeAnswerRequest request
    );

    FlagVO flagQuestion(
            AuthenticatedPrincipal principal,
            String exerciseSetId,
            String questionId,
            QuestionFlagRequest request
    );

    CoachHintVO hint(
            AuthenticatedPrincipal principal,
            String exerciseSetId,
            String questionId,
            CoachHintRequest request
    );

    PracticeSubmitVO submit(AuthenticatedPrincipal principal, String exerciseSetId, PracticeSubmitRequest request);

    CodeRunVO codeRun(
            AuthenticatedPrincipal principal,
            String exerciseSetId,
            String questionId,
            CodeRunRequest request
    );

    EvaluationReportVO report(AuthenticatedPrincipal principal, String attemptId);
}
