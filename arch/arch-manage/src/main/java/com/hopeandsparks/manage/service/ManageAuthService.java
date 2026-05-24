package com.hopeandsparks.manage.service;

import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.manage.dto.AdminLoginRequest;
import com.hopeandsparks.manage.dto.AdminRegisterRequest;
import com.hopeandsparks.manage.entity.AdminResource;
import com.hopeandsparks.manage.vo.AdminAuthTokenVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ManageAuthService {

    AdminAuthTokenVO register(AdminRegisterRequest request, HttpServletRequest servletRequest);

    AdminAuthTokenVO login(AdminLoginRequest request, HttpServletRequest servletRequest);

    void logout(String currentAccessToken);

    boolean canAccessManageUrl(AuthenticatedPrincipal principal, String requestPath);

    List<AdminResource> listAllEnabledResources();
}
