package com.hopeandsparks.agent.service;

import com.hopeandsparks.agent.dto.AgentMessageSendRequest;
import com.hopeandsparks.agent.dto.AgentSessionCreateRequest;
import com.hopeandsparks.agent.vo.AgentMessageSendVO;
import com.hopeandsparks.agent.vo.AgentMessageVO;
import com.hopeandsparks.agent.vo.AgentSessionVO;
import com.hopeandsparks.agent.vo.AgentStreamEventVO;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;

import java.util.List;

public interface AgentSessionService {

    AgentSessionVO createSession(AuthenticatedPrincipal principal, AgentSessionCreateRequest request);

    PageResponse<AgentMessageVO> listMessages(AuthenticatedPrincipal principal, String sessionId, long page, long pageSize);

    AgentMessageSendVO sendMessage(AuthenticatedPrincipal principal, String sessionId, AgentMessageSendRequest request);

    List<AgentStreamEventVO> streamEvents(AuthenticatedPrincipal principal, String sessionId, String messageId);
}
