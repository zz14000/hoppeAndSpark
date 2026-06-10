package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.dto.AiDisputeCreateRequest;
import com.hopeandsparks.agent.service.AiDisputeService;
import com.hopeandsparks.agent.vo.AiDisputeVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AiDisputeServiceImpl implements AiDisputeService {

    @Override
    public AiDisputeVO report(AuthenticatedPrincipal principal, AiDisputeCreateRequest request) {
        return new AiDisputeVO("dispute-" + System.currentTimeMillis(), "pending", request.reason(), LocalDateTime.now(), true);
    }
}
