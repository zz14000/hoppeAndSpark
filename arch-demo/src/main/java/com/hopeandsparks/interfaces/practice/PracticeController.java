package com.hopeandsparks.interfaces.practice;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接练习、测试、答题、Coach 提示、提交、代码题 AI 评阅和报告查询接口。
 */
@RestController
@RequestMapping("/api/v1/exercise-sets")
public class PracticeController {

    @GetMapping
    public ApiResponse<Map<String, Object>> sets(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "practice", "sets", Map.of(), query, null);
    }

    @GetMapping("/{exerciseSetId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String exerciseSetId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "practice", "detail", Map.of("exerciseSetId", exerciseSetId), Map.of(), null);
    }

    @GetMapping("/{exerciseSetId}/questions/{questionId}")
    public ApiResponse<Map<String, Object>> question(@PathVariable String exerciseSetId, @PathVariable String questionId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "practice", "question", Map.of("exerciseSetId", exerciseSetId, "questionId", questionId), Map.of(), null);
    }

    @PutMapping("/{exerciseSetId}/questions/{questionId}/answer")
    public ApiResponse<Map<String, Object>> answer(@PathVariable String exerciseSetId, @PathVariable String questionId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "practice", "answer", Map.of("exerciseSetId", exerciseSetId, "questionId", questionId), Map.of(), body);
    }

    @PutMapping("/{exerciseSetId}/questions/{questionId}/flag")
    public ApiResponse<Map<String, Object>> flag(@PathVariable String exerciseSetId, @PathVariable String questionId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "practice", "flag", Map.of("exerciseSetId", exerciseSetId, "questionId", questionId), Map.of(), body);
    }

    @PostMapping("/{exerciseSetId}/questions/{questionId}/hint")
    public ApiResponse<Map<String, Object>> hint(@PathVariable String exerciseSetId, @PathVariable String questionId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "practice", "hint", Map.of("exerciseSetId", exerciseSetId, "questionId", questionId), Map.of(), body);
    }

    @PostMapping("/{exerciseSetId}/submit")
    public ApiResponse<Map<String, Object>> submit(@PathVariable String exerciseSetId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "practice", "submit", Map.of("exerciseSetId", exerciseSetId), Map.of(), body);
    }

    @PostMapping("/{exerciseSetId}/questions/{questionId}/code-run")
    public ApiResponse<Map<String, Object>> codeRun(@PathVariable String exerciseSetId, @PathVariable String questionId, @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "practice", "codeRunAiReview", Map.of("exerciseSetId", exerciseSetId, "questionId", questionId), Map.of(), body);
    }

    @GetMapping("/attempts/{attemptId}/report")
    public ApiResponse<Map<String, Object>> report(@PathVariable String attemptId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "practice", "report", Map.of("attemptId", attemptId), Map.of(), null);
    }
}
