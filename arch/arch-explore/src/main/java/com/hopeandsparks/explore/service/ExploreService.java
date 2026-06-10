package com.hopeandsparks.explore.service;

import com.hopeandsparks.explore.dto.ExploreRequest;
import com.hopeandsparks.explore.dto.MindMapRequest;
import com.hopeandsparks.explore.vo.ExploreVO;
import com.hopeandsparks.explore.vo.MindMapVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;

public interface ExploreService {

    ExploreVO explore(AuthenticatedPrincipal principal, ExploreRequest request);

    ExploreVO detail(AuthenticatedPrincipal principal, String exploreId);

    MindMapVO createMindMap(AuthenticatedPrincipal principal, String exploreId, MindMapRequest request);
}
