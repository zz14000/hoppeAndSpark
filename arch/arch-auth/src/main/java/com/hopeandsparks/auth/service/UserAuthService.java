package com.hopeandsparks.auth.service;

import com.hopeandsparks.auth.dto.UserLoginRequest;
import com.hopeandsparks.auth.dto.UserLogoutRequest;
import com.hopeandsparks.auth.dto.UserRefreshRequest;
import com.hopeandsparks.auth.dto.UserRegisterRequest;
import com.hopeandsparks.auth.vo.UserAuthTokenVO;
import com.hopeandsparks.auth.vo.UserProfileVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;

public interface UserAuthService {

    UserAuthTokenVO register(UserRegisterRequest request, HttpServletRequest servletRequest);

    UserAuthTokenVO login(UserLoginRequest request, HttpServletRequest servletRequest);

    UserAuthTokenVO refresh(UserRefreshRequest request, String currentAccessToken);

    void logout(UserLogoutRequest request, String currentAccessToken);

    UserProfileVO currentUser(AuthenticatedPrincipal principal);
}
