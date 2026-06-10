package com.hopeandsparks.study.vo;

import java.util.List;

public record ResourceNetworkVO(String planId, String nodeId, List<String> resourceIds, boolean mock) {
}
