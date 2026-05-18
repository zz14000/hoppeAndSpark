package com.hopeandsparks.common.response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller 占位响应工具，专门用于当前“先把接口契约跑通”的阶段。
 *
 * <p>现在各 Controller 还没有接真实 Service 和 Mapper，所以会返回 module、action、
 * implemented=false 等字段，方便前端或接口测试确认路径、HTTP 方法和参数能打通。
 * 等业务 Service 实现后，逐步删除这些占位数据，改为返回真实 VO。</p>
 */
public final class PlaceholderData {

    private PlaceholderData() {
    }

    /**
     * 创建最简单的占位响应，只标识当前接口属于哪个模块、哪个动作。
     */
    public static Map<String, Object> of(String module, String action) {
        return of(module, action, Map.of());
    }

    /**
     * 创建带参数回显的占位响应，用于临时检查路径参数、查询参数、请求体是否正确传入。
     */
    public static Map<String, Object> of(String module, String action, Map<String, ?> values) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("module", module);
        data.put("action", action);
        data.put("implemented", false);
        data.put("note", "Controller contract is ready; service implementation will be wired later.");
        data.putAll(values);
        return data;
    }
}
