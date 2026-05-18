package com.hopeandsparks.common.web;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 占位响应参数构造工具，用来快速把路径参数、查询参数、请求体回显到返回值中。
 *
 * <p>Java 的 {@code Map.of} 不允许 value 为 null，当前 Controller 又允许请求体为空，
 * 所以用这个小工具构造 {@code LinkedHashMap}，避免占位接口因为空请求体报错。</p>
 */
public final class WebValueUtils {

    private WebValueUtils() {
    }

    public static Map<String, Object> values(Object... keysAndValues) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keysAndValues.length; i += 2) {
            values.put(String.valueOf(keysAndValues[i]), keysAndValues[i + 1]);
        }
        return values;
    }
}
