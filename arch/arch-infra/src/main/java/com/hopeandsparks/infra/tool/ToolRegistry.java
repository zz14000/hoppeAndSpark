package com.hopeandsparks.infra.tool;

import java.util.List;
import java.util.Map;

public interface ToolRegistry {

    Object call(String toolName, Map<String, Object> input);

    List<ToolCallRecord> recentCalls();
}
