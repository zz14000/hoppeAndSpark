package com.hopeandsparks.boot.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 全局应用信息接口，用于返回应用名、版本号、构建信息和运行环境。
 *
 * <p>它适合给后台或运维页面展示当前后端版本。后续接入 CI/CD 后，可以把 Git commit、
 * build time、active profile 等信息注入到这里。</p>
 */
@RestController
public class AppInfoController {

    @GetMapping("/api/v1/app/info")
    public ApiResponse<Map<String, Object>> info() {
        return ApiResponse.ok(PlaceholderData.of("boot", "appInfo", values(
                "name", "Hope and Sparks",
                "version", "0.1.0-SNAPSHOT"
        )));
    }
}
