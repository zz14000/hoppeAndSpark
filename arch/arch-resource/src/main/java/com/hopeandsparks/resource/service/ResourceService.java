package com.hopeandsparks.resource.service;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.resource.dto.ResourceExportRequest;
import com.hopeandsparks.resource.dto.ResourceFeedbackRequest;
import com.hopeandsparks.resource.dto.ResourceProgressUpdateRequest;
import com.hopeandsparks.resource.vo.ResourceCardVO;
import com.hopeandsparks.resource.vo.ResourceDetailVO;
import com.hopeandsparks.resource.vo.ResourceExportVO;
import com.hopeandsparks.resource.vo.ResourceFeedbackVO;
import com.hopeandsparks.resource.vo.ResourceProgressVO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ResourceService {

    PageResponse<ResourceCardVO> listResources(AuthenticatedPrincipal principal, Map<String, String> query);

    ResourceDetailVO detail(AuthenticatedPrincipal principal, String resourceId);

    ResourceProgressVO updateProgress(
            AuthenticatedPrincipal principal,
            String resourceId,
            ResourceProgressUpdateRequest request
    );

    ResourceExportVO exportResource(AuthenticatedPrincipal principal, String resourceId, ResourceExportRequest request);

    ResourceFeedbackVO feedback(AuthenticatedPrincipal principal, String resourceId, ResourceFeedbackRequest request);

    PageResponse<ResourceCardVO> listNodeResources(Long userId, Long nodeId, String type, long page, long pageSize);

    List<ResourceCardVO> listResourcesByNode(Long userId, Long nodeId, String type, int limit);

    Optional<Long> firstResourceIdByNode(Long nodeId);

    long countResourcesByNode(Long nodeId);

    long countLearnedResourcesByNode(Long userId, Long nodeId);

    long sumStudyDurationByNode(Long userId, Long nodeId);
}
