package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.AgentTaskResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Aggregator {

    public String answer(List<AgentTaskResult> results) {
        return results.stream()
                .map(AgentTaskResult::answerText)
                .filter(text -> text != null && !text.isBlank())
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("暂无可输出内容");
    }

    public String diagram(List<AgentTaskResult> results) {
        return results.stream()
                .map(AgentTaskResult::diagramScript)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .orElse("");
    }
}
