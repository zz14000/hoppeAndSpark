package com.hopeandsparks.profile.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * Spark 画像引导接口，负责画像问题、单轮回答、画像完成和重新构建画像。
 *
 * <p>后续实现时会调用画像 Service，结合用户回答、规则和 Agent 能力生成 {@code user_profile}，
 * 并把长期偏好、目标、薄弱点等动态信息沉淀到记忆表中。</p>
 */
@RestController
public class OnboardingController {

    @GetMapping("/api/v1/onboarding/questions")
    public ApiResponse<Map<String, Object>> questions() {
        return ApiResponse.ok(PlaceholderData.of("profile", "questions"));
    }

    @PostMapping("/api/v1/onboarding/answers")
    public ApiResponse<Map<String, Object>> answer(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("profile", "answer", values("request", request)));
    }

    @PostMapping("/api/v1/onboarding/complete")
    public ApiResponse<Map<String, Object>> complete(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("profile", "complete", values("request", request)));
    }

    @PostMapping("/api/v1/spark-profile/rebuild")
    public ApiResponse<Map<String, Object>> rebuild(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("profile", "rebuild", values("request", request)));
    }
}
