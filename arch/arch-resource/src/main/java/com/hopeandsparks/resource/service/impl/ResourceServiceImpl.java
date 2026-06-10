package com.hopeandsparks.resource.service.impl;

import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.resource.dto.ResourceExportRequest;
import com.hopeandsparks.resource.dto.ResourceFeedbackRequest;
import com.hopeandsparks.resource.dto.ResourceProgressUpdateRequest;
import com.hopeandsparks.resource.service.ResourceService;
import com.hopeandsparks.resource.vo.ResourceCardVO;
import com.hopeandsparks.resource.vo.ResourceDetailVO;
import com.hopeandsparks.resource.vo.ResourceExportVO;
import com.hopeandsparks.resource.vo.ResourceFeedbackVO;
import com.hopeandsparks.resource.vo.ResourceProgressVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Override
    public PageResponse<ResourceCardVO> listResources(AuthenticatedPrincipal principal, Map<String, String> query) {
        return PageResponse.of(1, 10, 1, List.of(new ResourceCardVO("mock-resource", "Mock Resource", "doc", "mock summary", true)));
    }

    @Override
    public ResourceDetailVO detail(AuthenticatedPrincipal principal, String resourceId) {
        return new ResourceDetailVO(resourceId, "Mock Resource", "doc", "mock summary", "mock content", true);
    }

    @Override
    public ResourceProgressVO updateProgress(AuthenticatedPrincipal principal, String resourceId, ResourceProgressUpdateRequest request) {
        int progress = request == null || request.progress() == null ? 0 : request.progress();
        String position = request == null ? "" : request.position();
        return new ResourceProgressVO(resourceId, progress, position, true);
    }

    @Override
    public ResourceExportVO exportResource(AuthenticatedPrincipal principal, String resourceId, ResourceExportRequest request) {
        String format = request == null || request.format() == null ? "pdf" : request.format();
        return new ResourceExportVO(resourceId, format, "mock://export/" + resourceId + "." + format, true);
    }

    @Override
    public ResourceFeedbackVO feedback(AuthenticatedPrincipal principal, String resourceId, ResourceFeedbackRequest request) {
        return new ResourceFeedbackVO(resourceId, "accepted", true);
    }
}
