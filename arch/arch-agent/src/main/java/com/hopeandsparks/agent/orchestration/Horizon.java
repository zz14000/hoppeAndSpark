package com.hopeandsparks.agent.orchestration;

import com.hopeandsparks.agent.dto.ReviewDecision;
import com.hopeandsparks.agent.enums.AgentName;
import com.hopeandsparks.agent.enums.ReviewStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Horizon {

    public ReviewDecision review(String answerText) {
        if (answerText == null || answerText.isBlank()) {
            return new ReviewDecision(ReviewStatus.BLOCK, "输出为空", List.of("empty_answer"), List.of("补充可返回内容"), AgentName.SAGE, false);
        }
        return new ReviewDecision(ReviewStatus.PUBLISH, "mock review passed", List.of(), List.of(), null, false);
    }
}
