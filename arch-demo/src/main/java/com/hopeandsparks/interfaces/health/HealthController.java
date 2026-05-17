package com.hopeandsparks.interfaces.health;


/**
 * 文件职责：HealthController 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\interfaces\health\HealthController.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.common.config.HopeProperties;
import com.hopeandsparks.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final HopeProperties hopeProperties;

    public HealthController(HopeProperties hopeProperties) {
        this.hopeProperties = hopeProperties;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> health(HttpServletRequest request) {
        return ApiResponse.success(Map.of(
            "status", "UP",
            "adapterMode", hopeProperties.getAdapterMode(),
            "agentMode", hopeProperties.getAgent().getMode(),
            "queueMode", hopeProperties.getQueue().getMode(),
            "fileMode", hopeProperties.getFile().getMode(),
            "vectorMode", hopeProperties.getVector().getMode()
        ), request.getAttribute("requestId").toString());
    }
}

