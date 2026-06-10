package com.hopeandsparks.infra.tool;

import java.util.Map;

public interface KnowledgeCacheGateway {

    Map<String, Object> writeCandidate(Map<String, Object> input);
}
