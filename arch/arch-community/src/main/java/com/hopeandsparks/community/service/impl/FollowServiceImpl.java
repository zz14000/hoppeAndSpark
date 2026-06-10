package com.hopeandsparks.community.service.impl;

import com.hopeandsparks.community.service.FollowService;
import com.hopeandsparks.community.vo.FollowResultVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

@Service
public class FollowServiceImpl implements FollowService {
    public FollowResultVO follow(AuthenticatedPrincipal principal, String userId) {
        return new FollowResultVO(userId, true, true);
    }
    public FollowResultVO unfollow(AuthenticatedPrincipal principal, String userId) {
        return new FollowResultVO(userId, false, true);
    }
}
