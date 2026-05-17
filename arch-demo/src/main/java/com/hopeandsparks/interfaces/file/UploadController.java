package com.hopeandsparks.interfaces.file;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.interfaces.support.MockApiResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件职责：承接上传凭证和上传完成回调接口，后续会对接 MinIO 与 sys_oss_file 落库。
 */
@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    @PostMapping("/token")
    public ApiResponse<Map<String, Object>> token(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "file", "uploadToken", Map.of(), Map.of(), body);
    }

    @PostMapping("/complete")
    public ApiResponse<Map<String, Object>> complete(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
        return MockApiResponseFactory.ok(request, "file", "uploadComplete", Map.of(), Map.of(), body);
    }
}
