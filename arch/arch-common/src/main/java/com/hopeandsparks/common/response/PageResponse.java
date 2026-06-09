package com.hopeandsparks.common.response;

import java.util.List;

/**
 * 统一分页响应结构，所有列表接口都按 page、pageSize、total、list 返回。
 *
 * <p>后续接入 MyBatis-Plus 分页查询时，可以把分页结果转换成这个 record，
 * 避免不同模块各自定义分页字段导致前端适配成本变高。</p>
 */
public record PageResponse<T>(long page, long pageSize, long total, List<T> list) {

    /**
     * 创建分页结果时做一点点兜底，避免 page、pageSize 出现 0 或负数。
     * 这里不放复杂逻辑，真正的分页查询仍然交给各业务模块处理。
     */
    public static <T> PageResponse<T> of(long page, long pageSize, long total, List<T> list) {
        long safePage = Math.max(page, 1);
        long safePageSize = Math.max(pageSize, 1);
        long safeTotal = Math.max(total, 0);
        List<T> safeList = list == null ? List.of() : list;
        return new PageResponse<>(safePage, safePageSize, safeTotal, safeList);
    }
}
