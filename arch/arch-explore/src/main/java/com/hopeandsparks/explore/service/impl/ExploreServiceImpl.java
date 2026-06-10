package com.hopeandsparks.explore.service.impl;

import com.hopeandsparks.explore.dto.ExploreRequest;
import com.hopeandsparks.explore.dto.MindMapRequest;
import com.hopeandsparks.explore.service.ExploreService;
import com.hopeandsparks.explore.vo.ExploreVO;
import com.hopeandsparks.explore.vo.MindMapVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExploreServiceImpl implements ExploreService {

    @Override
    public ExploreVO explore(AuthenticatedPrincipal principal, ExploreRequest request) {
        return new ExploreVO("explore-" + System.currentTimeMillis(), request.topic(), "mock explore summary", List.of(), true);
    }

    @Override
    public ExploreVO detail(AuthenticatedPrincipal principal, String exploreId) {
        return new ExploreVO(exploreId, "mock topic", "mock explore detail", List.of(), true);
    }

    @Override
    public MindMapVO createMindMap(AuthenticatedPrincipal principal, String exploreId, MindMapRequest request) {
        return new MindMapVO(exploreId, "mindmap\n  root((Mock Explore))\n    Topic\n    Resource", true);
    }
}
