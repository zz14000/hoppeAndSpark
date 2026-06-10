package com.hopeandsparks.knowledge.vo;

import java.util.List;

public record KnowledgeGraphVO(List<Node> nodes, List<Edge> edges, boolean mock) {

    public record Node(String id, String name, String type) {
    }

    public record Edge(String source, String target, String relationType) {
    }
}
