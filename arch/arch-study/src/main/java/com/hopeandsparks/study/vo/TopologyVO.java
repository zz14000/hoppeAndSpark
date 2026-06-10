package com.hopeandsparks.study.vo;

import java.util.List;

public record TopologyVO(List<String> nodes, List<String> edges, boolean mock) {
}
