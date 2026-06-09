package com.hopeandsparks.boot.controller;

import com.hopeandsparks.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 全局健康检查接口。
 * 这里只返回应用是否还活着，不写具体业务逻辑。
 */
@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of(
                "status", "UP",
                "time", LocalDateTime.now()
        ));
    }
}
