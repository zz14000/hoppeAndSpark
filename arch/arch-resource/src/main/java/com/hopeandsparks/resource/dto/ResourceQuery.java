package com.hopeandsparks.resource.dto;

/**
 * Internal query object for resource list filtering.
 */
public record ResourceQuery(
        String type,
        String keyword,
        Boolean verified,
        Long planId,
        Long nodeId
) {
}
