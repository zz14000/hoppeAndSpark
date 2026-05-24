package com.hopeandsparks.manage.entity;

public record AdminMenu(
        Long id,
        Long parentId,
        String name,
        String path,
        Integer level,
        Integer sortOrder
) {
}
