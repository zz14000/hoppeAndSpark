package com.hopeandsparks.practice.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.common.web.RequestContext;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 练习与评测接口，负责练习集、题目详情、作答保存、标记题目、Coach 提示、提交和报告。
 *
 * <p>MVP 的代码题不执行真实沙箱，只做文本级 mock 评阅；真实 Coach/Coze 接入后仍应放在 infra 网关后面。</p>
 */
@RestController
@RequestMapping("/api/v1/exercise-sets")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ExerciseSetCardVO>> sets(
            Authentication authentication,
            @RequestParam Map<String, String> query
    ) {
        return ApiResponse.ok(practiceService.listSets(principal(authentication), query));
    }

    @GetMapping("/{exerciseSetId}")
    public ApiResponse<ExerciseSetDetailVO> detail(Authentication authentication, @PathVariable String exerciseSetId) {
        return ApiResponse.ok(practiceService.detail(principal(authentication), exerciseSetId));
    }

    @GetMapping("/{exerciseSetId}/questions/{questionId}")
    public ApiResponse<QuestionAnswerDetailVO> question(
            Authentication authentication,
            @PathVariable String exerciseSetId,
            @PathVariable String questionId
    ) {
        return ApiResponse.ok(practiceService.question(principal(authentication), exerciseSetId, questionId));
    }

    @PutMapping("/{exerciseSetId}/questions/{questionId}/answer")
    public ApiResponse<AnswerSaveVO> answer(
            Authentication authentication,
            @PathVariable String exerciseSetId,
            @PathVariable String questionId,
            @RequestBody(required = false) PracticeAnswerRequest request
    ) {
        return ApiResponse.ok(
                "答案已保存",
                practiceService.saveAnswer(principal(authentication), exerciseSetId, questionId, request)
        );
    }

    @PutMapping("/{exerciseSetId}/questions/{questionId}/flag")
    public ApiResponse<FlagVO> flag(
            Authentication authentication,
            @PathVariable String exerciseSetId,
            @PathVariable String questionId,
            @RequestBody(required = false) QuestionFlagRequest request
    ) {
        return ApiResponse.ok(practiceService.flagQuestion(principal(authentication), exerciseSetId, questionId, request));
    }

    @PostMapping("/{exerciseSetId}/questions/{questionId}/hint")
    public ApiResponse<CoachHintVO> hint(
            Authentication authentication,
            @PathVariable String exerciseSetId,
            @PathVariable String questionId,
            @RequestBody(required = false) CoachHintRequest request
    ) {
        return ApiResponse.ok(practiceService.hint(principal(authentication), exerciseSetId, questionId, request));
    }

    @PostMapping("/{exerciseSetId}/submit")
    public ApiResponse<PracticeSubmitVO> submit(
            Authentication authentication,
            @PathVariable String exerciseSetId,
            @RequestBody(required = false) PracticeSubmitRequest request
    ) {
        PracticeSubmitVO result = practiceService.submit(principal(authentication), exerciseSetId, request);
        if (!Boolean.TRUE.equals(result.submitted())) {
            return new ApiResponse<>(409, result.message(), result, RequestContext.getRequestId());
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/{exerciseSetId}/questions/{questionId}/code-run")
    public ApiResponse<CodeRunVO> codeRun(
            Authentication authentication,
            @PathVariable String exerciseSetId,
            @PathVariable String questionId,
            @RequestBody(required = false) CodeRunRequest request
    ) {
        return ApiResponse.ok(practiceService.codeRun(principal(authentication), exerciseSetId, questionId, request));
    }

    @GetMapping("/attempts/{attemptId}/report")
    public ApiResponse<EvaluationReportVO> report(Authentication authentication, @PathVariable String attemptId) {
        return ApiResponse.ok(practiceService.report(principal(authentication), attemptId));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        return authentication == null ? null : (AuthenticatedPrincipal) authentication.getPrincipal();
    }
}
