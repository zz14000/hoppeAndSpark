package com.hopeandsparks.interfaces.profile;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接 Spark 首次引导与画像构建接口，后续会连接画像问答、画像生成和 Strict 计划初始化用例。
 */
@RestController
@RequestMapping("/api/v1")
public class OnboardingController {

    @GetMapping("/onboarding/questions")
    public ApiResponse<Map<String, Object>> questions(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "profile", "questions");
    }

    @PostMapping("/onboarding/answers")
    public ApiResponse<Map<String, Object>> answers(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "profile", "answers", Map.of(), Map.of(), body);
    }

    @PostMapping("/onboarding/complete")
    public ApiResponse<Map<String, Object>> complete(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "profile", "complete", Map.of(), Map.of(), body);
    }

    @PostMapping("/spark-profile/rebuild")
    public ApiResponse<Map<String, Object>> rebuild(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "profile", "rebuild", Map.of(), Map.of(), body);
    }
}
