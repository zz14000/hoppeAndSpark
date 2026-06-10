package com.hopeandsparks.manage.service.impl;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.manage.dto.DisputeHandleRequest;
import com.hopeandsparks.manage.service.ManageDisputeService;
import com.hopeandsparks.manage.vo.ManageDisputeHandleVO;
import com.hopeandsparks.manage.vo.ManageDisputeVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ManageDisputeServiceImpl implements ManageDisputeService {

    @Override
    public PageResponse<ManageDisputeVO> list(Map<String, String> query) {
        return PageResponse.of(1, 10, 1, List.of(new ManageDisputeVO("mock-dispute", "pending", "mock reason", true)));
    }

    @Override
    public ManageDisputeHandleVO handle(AuthenticatedPrincipal principal, String disputeId, DisputeHandleRequest request) {
        return new ManageDisputeHandleVO(new ManageDisputeVO(disputeId, request.status(), request.remark(), true), "handled", true);
    }
}
