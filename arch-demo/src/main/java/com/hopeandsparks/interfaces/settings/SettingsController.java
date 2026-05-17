package com.hopeandsparks.interfaces.settings;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接用户设置读取、更新和缓存清理接口。
 */
@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    @GetMapping
    public ApiResponse<Map<String, Object>> settings(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "settings", "get");
    }

    @PutMapping
    public ApiResponse<Map<String, Object>> update(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "settings", "update", Map.of(), Map.of(), body);
    }

    @PostMapping("/cache/clear")
    public ApiResponse<Map<String, Object>> clearCache(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "settings", "clearCache");
    }
}
