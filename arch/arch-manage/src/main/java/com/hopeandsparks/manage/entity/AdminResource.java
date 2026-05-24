package com.hopeandsparks.manage.entity;

public record AdminResource(
        Long id,
        String name,
        String code,
        String url
) {
    /**
     * 生成后台动态 URL 权限标识。
     * 该字符串需要和动态权限匹配逻辑保持一致，格式为资源ID加资源名称。
     */
    public String authority() {
        return id + ":" + name;
    }
}
