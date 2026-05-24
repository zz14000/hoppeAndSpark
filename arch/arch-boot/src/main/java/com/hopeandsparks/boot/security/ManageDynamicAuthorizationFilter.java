package com.hopeandsparks.boot.security;

import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.manage.service.ManageAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ManageDynamicAuthorizationFilter extends OncePerRequestFilter {

    private final ManageAuthService manageAuthService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 注入后台认证服务。
     * 过滤器会委托服务读取 RBAC 资源关系并判断当前管理员是否可以访问目标 URL。
     */
    public ManageDynamicAuthorizationFilter(ManageAuthService manageAuthService) {
        this.manageAuthService = manageAuthService;
    }

    /**
     * 对 Manage 后台请求执行动态 URL 授权。
     * 非后台路径直接放行，后台认证接口跳过资源校验，其余后台接口会根据 sys_admin_resource 做权限匹配。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!requiresManageAuthorization(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (principal.type() != IdentityType.ADMIN) {
            throw new AccessDeniedException("当前 token 不是后台管理员身份");
        }
        if (!manageAuthService.canAccessManageUrl(principal, request.getRequestURI())) {
            throw new AccessDeniedException("没有访问该后台资源的权限");
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 判断当前请求是否需要进入后台动态权限校验。
     * 只拦截 /api/v1/manage/**，并排除登录、初始化注册和退出登录等认证相关接口。
     */
    private boolean requiresManageAuthorization(HttpServletRequest request) {
        String path = request.getRequestURI();
        return pathMatcher.match("/api/v1/manage/**", path)
                && !pathMatcher.match("/api/v1/manage/auth/**", path);
    }
}
