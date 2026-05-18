package com.hopeandsparks.common.response;

import java.util.List;

/**
 * 统一分页响应结构，所有列表接口都按 page、pageSize、total、list 返回。
 *
 * <p>后续接入 MyBatis-Plus 分页查询时，可以把分页结果转换成这个 record，
 * 避免不同模块各自定义分页字段导致前端适配成本变高。</p>
 */
public record PageResponse<T>(long page, long pageSize, long total, List<T> list) {
}
