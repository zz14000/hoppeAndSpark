package com.hopeandsparks.resource.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 视频资源接口，负责视频详情、选集、播放进度和字幕/AI 提示。
 *
 * <p>播放进度会写入用户学习记录，字幕可以进入 Chroma 作为视频内容的向量检索来源。
 * 当前先保留接口路径，后续再接视频元数据、字幕和学习进度 Service。</p>
 */
@RestController
public class VideoController {

    @GetMapping("/api/v1/videos/{videoId}")
    public ApiResponse<Map<String, Object>> video(@PathVariable String videoId) {
        return ApiResponse.ok(PlaceholderData.of("resource", "video", values("videoId", videoId)));
    }

    @GetMapping("/api/v1/videos/{videoId}/episodes")
    public ApiResponse<Map<String, Object>> episodes(@PathVariable String videoId) {
        return ApiResponse.ok(PlaceholderData.of("resource", "episodes", values("videoId", videoId)));
    }

    @PutMapping("/api/v1/videos/{videoId}/watch-progress")
    public ApiResponse<Map<String, Object>> watchProgress(
            @PathVariable String videoId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("resource", "watchProgress", values("videoId", videoId, "request", request)));
    }

    @GetMapping("/api/v1/videos/{videoId}/transcripts")
    public ApiResponse<Map<String, Object>> transcripts(@PathVariable String videoId) {
        return ApiResponse.ok(PlaceholderData.of("resource", "transcripts", values("videoId", videoId)));
    }
}
