package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.service.AgentOutputValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StrictAgentOutputValidator implements AgentOutputValidator {

    private static final Map<AgentName, List<String>> REQUIRED_KEYS = Map.of(
            AgentName.SAGE, List.of("summary", "citations", "knowledgePoints"),
            AgentName.COACH, List.of("steps", "hints", "commonMistakes"),
            AgentName.NEBULA, List.of("diagramType", "diagramIntent", "diagramScript", "nodeSummary", "renderHint"),
            AgentName.STRICT, List.of("planItems", "checkpoints", "adaptationRules", "planSummary"),
            AgentName.RESOURCE, List.of("resourceBundle", "selectionReason", "videoResources", "referenceResources", "practiceResources", "qualityFlags")
    );

    @Override
    public void validate(AgentTaskResult result) {
        if (result == null) {
            throw new IllegalStateException("Agent output is null");
        }
        List<String> required = REQUIRED_KEYS.get(result.sourceAgent());
        if (required == null || required.isEmpty()) {
            return;
        }
        Map<String, Object> payload = result.structuredPayload();
        if (payload == null || payload.isEmpty()) {
            throw new IllegalStateException("Agent output schema validation failed for " + result.sourceAgent() + ": payload is empty");
        }
        List<String> missing = required.stream()
                .filter(key -> !payload.containsKey(key) || payload.get(key) == null)
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Agent output schema validation failed for " + result.sourceAgent() + ": missing keys " + missing);
        }
    }
}
