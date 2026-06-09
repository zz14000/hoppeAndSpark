package com.hopeandsparks.auth.service;

import com.hopeandsparks.auth.dto.PasswordResetConfirmRequest;
import com.hopeandsparks.auth.dto.PasswordResetRequest;
import com.hopeandsparks.auth.dto.UserChangeEmailRequest;
import com.hopeandsparks.auth.dto.UserChangePasswordRequest;
import com.hopeandsparks.auth.dto.UserLoginRequest;
import com.hopeandsparks.auth.dto.UserLogoutRequest;
import com.hopeandsparks.auth.dto.UserRefreshRequest;
import com.hopeandsparks.auth.dto.UserRegisterRequest;
import com.hopeandsparks.auth.dto.UserSettingsUpdateRequest;
import com.hopeandsparks.auth.vo.PasswordResetTokenVO;
import com.hopeandsparks.auth.vo.UserAuthTokenVO;
import com.hopeandsparks.auth.vo.UserDeviceVO;
import com.hopeandsparks.auth.vo.UserProfileVO;
import com.hopeandsparks.auth.vo.UserSettingsVO;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserAuthService {

    UserAuthTokenVO register(UserRegisterRequest request, HttpServletRequest servletRequest);

    UserAuthTokenVO login(UserLoginRequest request, HttpServletRequest servletRequest);

    UserAuthTokenVO refresh(UserRefreshRequest request, String currentAccessToken);

    void logout(UserLogoutRequest request, String currentAccessToken);

    UserProfileVO currentUser(AuthenticatedPrincipal principal);

    PasswordResetTokenVO requestPasswordReset(PasswordResetRequest request);

    void confirmPasswordReset(PasswordResetConfirmRequest request);

    List<UserDeviceVO> listDevices(AuthenticatedPrincipal principal);

    void offlineDevice(AuthenticatedPrincipal principal, String sessionId);

    void changePassword(AuthenticatedPrincipal principal, UserChangePasswordRequest request);

    void changeEmail(AuthenticatedPrincipal principal, UserChangeEmailRequest request);

    UserSettingsVO getSettings(AuthenticatedPrincipal principal);

    UserSettingsVO updateSettings(AuthenticatedPrincipal principal, UserSettingsUpdateRequest request);

    void clearCache(AuthenticatedPrincipal principal);
}
