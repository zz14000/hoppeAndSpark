package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.AiDisputeCreateRequest;
import com.hopeandsparks.agent.entity.AiDispute;
import com.hopeandsparks.agent.service.AiDisputeService;
import com.hopeandsparks.agent.vo.AiDisputeVO;
import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 争议上报的 W3 内存实现，只负责收集工单，不在这里做审核决策。
 */
@Service
public class InMemoryAiDisputeService implements AiDisputeService {

    private final Map<String, AiDispute> disputes = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1001);

    @Override
    public AiDisputeVO report(AuthenticatedPrincipal principal, AiDisputeCreateRequest request) {
        Long userId = requireUserId(principal);
        if (request == null) {
            throw new BusinessException(400, "请求体不能为空");
        }
        String issueType = firstText(request.issueType(), request.reason());
        if (issueType == null || issueType.isBlank()) {
            issueType = "unknown";
        }

        AiDispute dispute = new AiDispute();
        dispute.setDisputeId("aid_" + sequence.getAndIncrement());
        dispute.setUserId(userId);
        dispute.setTargetType(request.targetType());
        dispute.setTargetId(request.targetId());
        dispute.setIssueType(issueType);
        dispute.setDescription(request.description());
        dispute.setEvidence(request.evidence() == null ? List.of() : request.evidence());
        dispute.setStatus("pending");
        dispute.setCreatedAt(LocalDateTime.now());

        disputes.put(dispute.getDisputeId(), dispute);
        return toVO(dispute);
    }

    private AiDisputeVO toVO(AiDispute dispute) {
        return new AiDisputeVO(
                dispute.getDisputeId(),
                dispute.getTargetType(),
                dispute.getTargetId(),
                dispute.getIssueType(),
                dispute.getStatus(),
                dispute.getCreatedAt()
        );
    }

    private Long requireUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            throw new BusinessException(401, "请先登录前台账号");
        }
        return principal.id();
    }

    private String firstText(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }
}
