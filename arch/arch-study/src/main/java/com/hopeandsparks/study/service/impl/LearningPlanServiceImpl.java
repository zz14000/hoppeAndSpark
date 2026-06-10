package com.hopeandsparks.study.service.impl;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.resource.vo.ResourceCardVO;
import com.hopeandsparks.study.dto.PlanAdjustRequest;
import com.hopeandsparks.study.dto.PlanGenerateRequest;
import com.hopeandsparks.study.service.LearningPlanService;
import com.hopeandsparks.study.vo.LearningPlanVO;
import com.hopeandsparks.study.vo.ResourceNetworkVO;
import com.hopeandsparks.study.vo.TopologyVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningPlanServiceImpl implements LearningPlanService {

    @Override
    public LearningPlanVO currentPlan(AuthenticatedPrincipal principal) {
        return plan("current-plan", "Mock Current Plan");
    }

    @Override
    public LearningPlanVO generatePlan(AuthenticatedPrincipal principal, PlanGenerateRequest request) {
        String course = request == null || request.courseName() == null ? "课程" : request.courseName();
        return plan("generated-plan", course + " 学习计划");
    }

    @Override
    public LearningPlanVO adjustPlan(AuthenticatedPrincipal principal, String planId, PlanAdjustRequest request) {
        return plan(planId, "Mock Adjusted Plan");
    }

    @Override
    public TopologyVO topology(AuthenticatedPrincipal principal, String planId) {
        return new TopologyVO(List.of("start", "topic", "review"), List.of("start->topic", "topic->review"), true);
    }

    @Override
    public ResourceNetworkVO resourceNetwork(AuthenticatedPrincipal principal, String planId, String nodeId) {
        return new ResourceNetworkVO(planId, nodeId, List.of("mock-resource"), true);
    }

    @Override
    public PageResponse<ResourceCardVO> nodeResources(AuthenticatedPrincipal principal, String planId, String nodeId, String type, long page, long pageSize) {
        return PageResponse.of(page, pageSize, 1, List.of(new ResourceCardVO("mock-resource", "Mock Resource", type, "mock", true)));
    }

    private LearningPlanVO plan(String id, String title) {
        return new LearningPlanVO(id, title, "mock plan summary", List.of("学习知识点", "完成练习", "复盘错题"), true);
    }
}
