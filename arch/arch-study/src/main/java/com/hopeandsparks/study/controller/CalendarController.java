package com.hopeandsparks.study.controller;

import com.hopeandsparks.common.response.ApiResponse;
import com.hopeandsparks.common.response.PlaceholderData;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.hopeandsparks.common.web.WebValueUtils.values;

/**
 * 学习日历接口，负责查询、新建、更新和删除学习日程。
 *
 * <p>日程可以来自用户手动创建，也可以由 Strict 生成的学习任务同步而来。后续会和
 * {@code study_task}、提醒通知、学习记录联动。</p>
 */
@RestController
@RequestMapping("/api/v1/calendar/events")
public class CalendarController {

    @GetMapping
    public ApiResponse<Map<String, Object>> events(@RequestParam Map<String, String> query) {
        return ApiResponse.ok(PlaceholderData.of("study", "events", values("query", query)));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createEvent(@RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("study", "createEvent", values("request", request)));
    }

    @PutMapping("/{eventId}")
    public ApiResponse<Map<String, Object>> updateEvent(
            @PathVariable String eventId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ApiResponse.ok(PlaceholderData.of("study", "updateEvent", values("eventId", eventId, "request", request)));
    }

    @DeleteMapping("/{eventId}")
    public ApiResponse<Map<String, Object>> deleteEvent(@PathVariable String eventId) {
        return ApiResponse.ok(PlaceholderData.of("study", "deleteEvent", values("eventId", eventId)));
    }
}
