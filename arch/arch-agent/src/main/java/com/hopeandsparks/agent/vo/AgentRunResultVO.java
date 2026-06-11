package com.hopeandsparks.agent.vo;

import com.hopeandsparks.agent.dto.AgentTaskResult;
import com.hopeandsparks.agent.dto.AgentCheckpointSnapshot;
import com.hopeandsparks.agent.dto.AgentRunDebugVO;
import com.hopeandsparks.agent.dto.FinalAnswerEnvelope;
import com.hopeandsparks.agent.dto.ResourceBundle;
import com.hopeandsparks.agent.dto.ResourceSelectionDecision;
import com.hopeandsparks.agent.dto.RetrievalBundle;
import com.hopeandsparks.agent.dto.ReviewDecision;
import com.hopeandsparks.infra.tool.ToolCallRecord;

import java.util.List;
import java.util.Map;

public record AgentRunResultVO(
        String runId,
        long stateVersion,
        String status,
        String intent,
        String reviewStatus,
        String finalAnswer,
        String answerSummary,
        List<String> stepList,
        String diagramScript,
        String diagramImagePath,
        List<String> citations,
        List<String> learningPlan,
        List<String> memoryUpdates,
        List<String> cacheCandidates,
        List<AgentTaskResult> taskResults,
        ReviewDecision reviewDecision,
        List<ToolCallRecord> toolTrace,
        Map<String, Object> artifacts,
        ResourceBundle resourceBundle,
        ResourceSelectionDecision resourceDecision,
        List<String> resourceTrace,
        List<String> qualityFlags,
        RetrievalBundle retrieval,
        List<AgentCheckpointSnapshot> checkpoints,
        FinalAnswerEnvelope answerEnvelope,
        AgentRunDebugVO debug,
        Map<String, Object> payload,
        boolean mock
) {
}
