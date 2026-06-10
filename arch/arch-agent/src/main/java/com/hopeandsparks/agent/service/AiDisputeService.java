package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.AiDisputeCreateRequest;
import com.hopeandsparks.agent.vo.AiDisputeVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;

public interface AiDisputeService {

    AiDisputeVO report(AuthenticatedPrincipal principal, AiDisputeCreateRequest request);
}
