package com.hopeandsparks.study.service;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.resource.vo.ResourceCardVO;
import com.hopeandsparks.study.dto.PlanAdjustRequest;
import com.hopeandsparks.study.dto.PlanGenerateRequest;
import com.hopeandsparks.study.vo.LearningPlanVO;
import com.hopeandsparks.study.vo.ResourceNetworkVO;
import com.hopeandsparks.study.vo.TopologyVO;

public interface LearningPlanService {

    LearningPlanVO currentPlan(AuthenticatedPrincipal principal);

    LearningPlanVO generatePlan(AuthenticatedPrincipal principal, PlanGenerateRequest request);

    LearningPlanVO adjustPlan(AuthenticatedPrincipal principal, String planId, PlanAdjustRequest request);

    TopologyVO topology(AuthenticatedPrincipal principal, String planId);

    ResourceNetworkVO resourceNetwork(AuthenticatedPrincipal principal, String planId, String nodeId);

    PageResponse<ResourceCardVO> nodeResources(
            AuthenticatedPrincipal principal,
            String planId,
            String nodeId,
            String type,
            long page,
            long pageSize
    );
}
