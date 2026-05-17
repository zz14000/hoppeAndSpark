package com.hopeandsparks.interfaces.user;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接用户资料、个人中心、收藏、设备安全和账号设置相关接口。
 */
@RestController
@RequestMapping("/api/v1")
public class UserController {

    @GetMapping("/user/current")
    public ApiResponse<Map<String, Object>> current(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "current");
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<Map<String, Object>> userDetail(@PathVariable String userId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "detail", Map.of("userId", userId), Map.of(), null);
    }

    @PutMapping("/user/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "updateProfile", Map.of(), Map.of(), body);
    }

    @GetMapping("/user/learning-stats")
    public ApiResponse<Map<String, Object>> learningStats(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "learningStats");
    }

    @GetMapping("/user/collections")
    public ApiResponse<Map<String, Object>> collections(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "collections", Map.of(), query, null);
    }

    @PostMapping("/user/collections")
    public ApiResponse<Map<String, Object>> collect(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "collect", Map.of(), Map.of(), body);
    }

    @DeleteMapping("/user/collections/{collectionId}")
    public ApiResponse<Map<String, Object>> removeCollection(@PathVariable String collectionId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "removeCollection", Map.of("collectionId", collectionId), Map.of(), null);
    }

    @GetMapping("/user/devices")
    public ApiResponse<Map<String, Object>> devices(HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "devices");
    }

    @DeleteMapping("/user/devices/{deviceId}")
    public ApiResponse<Map<String, Object>> removeDevice(@PathVariable String deviceId, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "removeDevice", Map.of("deviceId", deviceId), Map.of(), null);
    }

    @PutMapping("/user/password")
    public ApiResponse<Map<String, Object>> updatePassword(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "updatePassword", Map.of(), Map.of(), body);
    }

    @PutMapping("/user/email")
    public ApiResponse<Map<String, Object>> updateEmail(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "user", "updateEmail", Map.of(), Map.of(), body);
    }
}
