package com.hopeandsparks.resource.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 拓展阅读和代码案例接口，用于打开资源详情页中的阅读材料和代码案例。
 *
 * <p>这类接口本质上仍属于学习资源模块，只是前端页面形态不同。后续可以根据资源类型
 * 查询对应的资源版本、正文内容、附件文件和引用来源。</p>
 */
@RestController
public class ReadingController {

    @GetMapping("/api/v1/readings/{readingId}")
    public ApiResponse<Map<String, Object>> reading(@PathVariable String readingId) {
        return ApiResponse.ok(PlaceholderData.of("resource", "reading", values("readingId", readingId)));
    }

    @GetMapping("/api/v1/code-cases/{caseId}")
    public ApiResponse<Map<String, Object>> codeCase(@PathVariable String caseId) {
        return ApiResponse.ok(PlaceholderData.of("resource", "codeCase", values("caseId", caseId)));
    }
}
