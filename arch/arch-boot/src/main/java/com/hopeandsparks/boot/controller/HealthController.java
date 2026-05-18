package com.hopeandsparks.boot.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 全局健康检查接口，不属于具体业务模块。
 *
 * <p>这个接口用于本地开发、部署平台或网关探活。后续可以在这里扩展数据库、Redis、
 * MinIO、Chroma 等依赖状态，但不要放登录、资源、知识库等业务逻辑。</p>
 */
@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(PlaceholderData.of("boot", "health", values("status", "UP")));
    }
}
