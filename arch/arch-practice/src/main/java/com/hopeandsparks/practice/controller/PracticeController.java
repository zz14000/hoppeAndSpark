package com.hopeandsparks.practice.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 练习与评测接口，负责练习集、题目详情、作答保存、标记题目、Coach 提示、提交和报告。
 *
 * <p>MVP 的代码题不执行真实沙箱，先保存代码文本并调用 AI 评阅。后续这里会接
 * {@code practice_set}、{@code question_bank}、{@code user_question_record}
 * 和 {@code evaluation_report} 等表。</p>
 */
@RestController
@RequestMapping("/api/v1/exercise-sets")
public class PracticeController {

    @GetMapping
    public ApiResponse<Map<String, Object>> sets(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("practice", "sets", values("query", query)));
    }

    @GetMapping("/{exerciseSetId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String exerciseSetId) {
        return ApiResponse.ok(PlaceholderData.of("practice", "detail", values("exerciseSetId", exerciseSetId)));
    }

    @GetMapping("/{exerciseSetId}/questions/{questionId}")
    public ApiResponse<Map<String, Object>> question(@PathVariable String exerciseSetId, @PathVariable String questionId) {
        return ApiResponse.ok(PlaceholderData.of("practice", "question", values("exerciseSetId", exerciseSetId, "questionId", questionId)));
    }

    @PutMapping("/{exerciseSetId}/questions/{questionId}/answer")
    public ApiResponse<Map<String, Object>> answer(
            @PathVariable String exerciseSetId,
            @PathVariable String questionId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("practice", "answer", values("exerciseSetId", exerciseSetId, "questionId", questionId, "request", request)));
    }

    @PutMapping("/{exerciseSetId}/questions/{questionId}/flag")
    public ApiResponse<Map<String, Object>> flag(
            @PathVariable String exerciseSetId,
            @PathVariable String questionId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("practice", "flag", values("exerciseSetId", exerciseSetId, "questionId", questionId, "request", request)));
    }

    @PostMapping("/{exerciseSetId}/questions/{questionId}/hint")
    public ApiResponse<Map<String, Object>> hint(
            @PathVariable String exerciseSetId,
            @PathVariable String questionId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("practice", "hint", values("exerciseSetId", exerciseSetId, "questionId", questionId, "request", request)));
    }

    @PostMapping("/{exerciseSetId}/submit")
    public ApiResponse<Map<String, Object>> submit(
            @PathVariable String exerciseSetId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("practice", "submit", values("exerciseSetId", exerciseSetId, "request", request)));
    }

    @PostMapping("/{exerciseSetId}/questions/{questionId}/code-run")
    public ApiResponse<Map<String, Object>> codeRun(
            @PathVariable String exerciseSetId,
            @PathVariable String questionId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("practice", "codeRun", values("exerciseSetId", exerciseSetId, "questionId", questionId, "request", request)));
    }

    @GetMapping("/attempts/{attemptId}/report")
    public ApiResponse<Map<String, Object>> report(@PathVariable String attemptId) {
        return ApiResponse.ok(PlaceholderData.of("practice", "report", values("attemptId", attemptId)));
    }
}
