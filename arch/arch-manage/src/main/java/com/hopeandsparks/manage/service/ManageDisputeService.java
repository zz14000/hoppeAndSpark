package com.hopeandsparks.manage.service;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.manage.dto.DisputeHandleRequest;
import com.hopeandsparks.manage.vo.ManageDisputeHandleVO;
import com.hopeandsparks.manage.vo.ManageDisputeVO;

import java.util.Map;

/**
 * Manage service for AI dispute tickets.
 */
public interface ManageDisputeService {

    PageResponse<ManageDisputeVO> list(Map<String, String> query);

    ManageDisputeHandleVO handle(AuthenticatedPrincipal principal, String disputeId, DisputeHandleRequest request);
}
