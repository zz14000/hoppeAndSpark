package com.hopeandsparks.manage.service.impl;

import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.infra.redis.AuthTokenRedisStore;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.infra.security.JwtSubject;
import com.hopeandsparks.infra.security.JwtTokenService;
import com.hopeandsparks.infra.security.SecurityProperties;
import com.hopeandsparks.manage.dto.AdminLoginRequest;
import com.hopeandsparks.manage.dto.AdminRegisterRequest;
import com.hopeandsparks.manage.entity.AdminAccount;
import com.hopeandsparks.manage.entity.AdminMenu;
import com.hopeandsparks.manage.entity.AdminResource;
import com.hopeandsparks.manage.repository.AdminAuthRepository;
import com.hopeandsparks.manage.service.ManageAuthService;
import com.hopeandsparks.manage.vo.AdminAuthTokenVO;
import com.hopeandsparks.manage.vo.AdminMenuVO;
import com.hopeandsparks.manage.vo.AdminProfileVO;
import com.hopeandsparks.manage.vo.AdminResourceVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ManageAuthServiceImpl implements ManageAuthService {

    private static final int STATUS_ENABLED = 1;

    private final AdminAuthRepository adminAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthTokenRedisStore tokenRedisStore;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 装配后台认证、JWT 签发、Redis 登录态和 RBAC 资源查询依赖。
     * 该实现负责把 sys_admin、角色资源关系和动态 URL 权限组合成后台认证能力。
     */
    public ManageAuthServiceImpl(
            AdminAuthRepository adminAuthRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            AuthTokenRedisStore tokenRedisStore,
            SecurityProperties securityProperties
    ) {
        this.adminAuthRepository = adminAuthRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.tokenRedisStore = tokenRedisStore;
        this.securityProperties = securityProperties;
    }

    /**
     * 初始化注册第一个后台管理员。
     * 仅在 sys_admin 为空时允许执行，注册成功后会创建或复用超级管理员角色，并授予全部菜单和 Controller 资源。
     */
    @Override
    @Transactional
    public AdminAuthTokenVO register(AdminRegisterRequest request, HttpServletRequest servletRequest) {
        if (adminAuthRepository.countAdmins() > 0) {
            throw new BusinessException(403, "后台管理员初始化已完成，请在管理员管理中创建新账号");
        }
        if (adminAuthRepository.existsByUsername(request.username())) {
            throw new BusinessException(409, "管理员账号已存在");
        }
        Long adminId = adminAuthRepository.insertAdmin(request, passwordEncoder.encode(request.password()));
        adminAuthRepository.grantSuperAdmin(adminId);
        AdminAccount admin = adminAuthRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(500, "管理员创建后读取失败"));
        return issueToken(admin);
    }

    /**
     * 后台管理员账号密码登录。
     * 会校验管理员存在、账号启用状态和 BCrypt 密码，成功后签发后台 JWT 并返回菜单与资源权限。
     */
    @Override
    public AdminAuthTokenVO login(AdminLoginRequest request, HttpServletRequest servletRequest) {
        AdminAccount admin = adminAuthRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(401, "管理员账号或密码错误"));
        ensureAdminCanLogin(admin);
        if (!passwordEncoder.matches(request.password(), admin.passwordHash())) {
            throw new BusinessException(401, "管理员账号或密码错误");
        }
        return issueToken(admin);
    }

    /**
     * 后台管理员退出登录。
     * 删除当前 access token 对应的 Redis 登录态，使该 token 立即失效。
     */
    @Override
    public void logout(String currentAccessToken) {
        if (currentAccessToken != null && !currentAccessToken.isBlank()) {
            tokenRedisStore.delete(currentAccessToken, IdentityType.ADMIN);
        }
    }

    /**
     * 判断后台管理员是否可以访问指定 Manage URL。
     * 先根据 sys_admin_resource 中的 URL 规则找出接口所需资源，再和当前管理员通过角色拥有的资源做匹配。
     */
    @Override
    public boolean canAccessManageUrl(AuthenticatedPrincipal principal, String requestPath) {
        if (principal == null || principal.type() != IdentityType.ADMIN) {
            return false;
        }
        List<AdminResource> requiredResources = listAllEnabledResources().stream()
                .filter(resource -> matches(resource.url(), requestPath))
                .toList();
        if (requiredResources.isEmpty()) {
            return true;
        }
        Set<String> grantedAuthorities = adminAuthRepository.listGrantedResources(principal.id()).stream()
                .map(AdminResource::authority)
                .collect(Collectors.toSet());
        return requiredResources.stream()
                .map(AdminResource::authority)
                .anyMatch(grantedAuthorities::contains);
    }

    /**
     * 查询全部启用的后台 Controller 资源。
     * 动态 URL 权限过滤会用这份资源表把请求路径映射到所需权限标识。
     */
    @Override
    public List<AdminResource> listAllEnabledResources() {
        return adminAuthRepository.listAllEnabledResources();
    }

    /**
     * 为后台管理员签发 JWT 并组装登录响应。
     * JWT 写入 Redis 的 auth:admin:token:{token} 登录态，同时响应中带回角色、菜单和资源权限给后台前端使用。
     */
    private AdminAuthTokenVO issueToken(AdminAccount admin) {
        JwtSubject subject = new JwtSubject(admin.id(), admin.username(), IdentityType.ADMIN, null);
        String token = jwtTokenService.generateToken(subject, securityProperties.getJwt().getAdminExpiration());
        tokenRedisStore.save(token, subject, securityProperties.getJwt().getAdminExpiration());
        List<String> roles = adminAuthRepository.listRoleKeys(admin.id());
        List<AdminMenuVO> menus = adminAuthRepository.listMenus(admin.id()).stream()
                .map(this::toMenuVO)
                .toList();
        List<AdminResourceVO> resources = adminAuthRepository.listGrantedResources(admin.id()).stream()
                .map(this::toResourceVO)
                .toList();
        return new AdminAuthTokenVO(
                token,
                securityProperties.getJwt().getTokenPrefix(),
                securityProperties.getJwt().getAdminExpiration().toSeconds(),
                new AdminProfileVO(String.valueOf(admin.id()), admin.username(), admin.realName(), roles),
                menus,
                resources
        );
    }

    /**
     * 校验后台管理员账号是否允许登录。
     * 当前只检查启用状态，后续可在这里扩展锁定、过期、二次验证等后台安全策略。
     */
    private void ensureAdminCanLogin(AdminAccount admin) {
        if (admin.adminStatus() != STATUS_ENABLED) {
            throw new BusinessException(403, "管理员账号已禁用");
        }
    }

    /**
     * 判断资源表中的 Controller 基础路径是否命中当前请求路径。
     * 同时支持精确匹配和基础路径下的任意子路径，便于一个资源保护一个 Controller。
     */
    private boolean matches(String resourceUrl, String requestPath) {
        if (resourceUrl == null || resourceUrl.isBlank()) {
            return false;
        }
        String normalizedResourceUrl = resourceUrl.endsWith("/") ? resourceUrl.substring(0, resourceUrl.length() - 1) : resourceUrl;
        return pathMatcher.match(normalizedResourceUrl, requestPath)
                || pathMatcher.match(normalizedResourceUrl + "/**", requestPath);
    }

    /**
     * 把后台菜单实体转换成前端菜单响应对象。
     * 响应中 ID 使用字符串，保持和项目 API 约定一致。
     */
    private AdminMenuVO toMenuVO(AdminMenu menu) {
        return new AdminMenuVO(
                String.valueOf(menu.id()),
                String.valueOf(menu.parentId()),
                menu.name(),
                menu.path(),
                menu.level(),
                menu.sortOrder()
        );
    }

    /**
     * 把后台 Controller 资源实体转换成权限响应对象。
     * 同时生成 Spring Security 使用的动态权限标识，格式为资源ID加资源名称。
     */
    private AdminResourceVO toResourceVO(AdminResource resource) {
        return new AdminResourceVO(
                String.valueOf(resource.id()),
                resource.name(),
                resource.code(),
                resource.url(),
                resource.authority()
        );
    }
}
