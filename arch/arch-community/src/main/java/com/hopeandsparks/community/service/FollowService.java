package com.hopeandsparks.community.service;

import com.hopeandsparks.community.vo.FollowResultVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;

/**
 * Community follow operations.
 */
public interface FollowService {

    FollowResultVO follow(AuthenticatedPrincipal principal, String userId);

    FollowResultVO unfollow(AuthenticatedPrincipal principal, String userId);
}
