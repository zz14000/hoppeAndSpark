package com.hopeandsparks.boot.controller;

import com.hopeandsparks.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

/**
 * 应用信息接口。
 * 给前端、后台或部署平台确认当前后端名称、版本和 profile。
 */
@RestController
public class AppInfoController {

    private final Environment environment;
    private final String appName;

    public AppInfoController(
            Environment environment,
            @Value("${spring.application.name:hope-and-sparks}") String appName
    ) {
        this.environment = environment;
        this.appName = appName;
    }

    @GetMapping("/api/v1/app/info")
    public ApiResponse<Map<String, Object>> info() {
        return ApiResponse.ok(Map.of(
                "name", appName,
                "displayName", "Hope and Sparks",
                "version", "0.1.0-SNAPSHOT",
                "profiles", Arrays.asList(environment.getActiveProfiles())
        ));
    }
}
