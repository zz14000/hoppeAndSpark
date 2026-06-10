package com.hopeandsparks.explore.vo;

import java.util.List;

public record ExploreVO(String id, String topic, String summary, List<String> relatedNodes, boolean mock) {
}
