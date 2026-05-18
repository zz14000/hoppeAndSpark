package com.hopeandsparks.knowledge.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 技能树接口，用于展示用户知识技能树，以及点亮某个技能节点。
 *
 * <p>点亮动作后续会结合 {@code user_knowledge_progress}、练习结果和学习记录判断是否允许点亮，
 * 当前先保留 API 契约，便于前端技能树页面联调。</p>
 */
@RestController
public class SkillTreeController {

    @GetMapping("/api/v1/skill-tree")
    public ApiResponse<Map<String, Object>> skillTree() {
        return ApiResponse.ok(PlaceholderData.of("knowledge", "skillTree"));
    }

    @PostMapping("/api/v1/skill-tree/nodes/{nodeId}/light-up")
    public ApiResponse<Map<String, Object>> lightUp(
            @PathVariable String nodeId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("knowledge", "lightUp", values("nodeId", nodeId, "request", request)));
    }
}
