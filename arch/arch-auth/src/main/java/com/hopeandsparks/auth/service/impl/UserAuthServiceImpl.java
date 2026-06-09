package com.hopeandsparks.auth.service.impl;

import com.hopeandsparks.auth.dto.PasswordResetConfirmRequest;
import com.hopeandsparks.auth.dto.PasswordResetRequest;
import com.hopeandsparks.auth.dto.UserChangeEmailRequest;
import com.hopeandsparks.auth.dto.UserChangePasswordRequest;
import com.hopeandsparks.auth.dto.UserLoginRequest;
import com.hopeandsparks.auth.dto.UserLogoutRequest;
import com.hopeandsparks.auth.dto.UserRefreshRequest;
import com.hopeandsparks.auth.dto.UserRegisterRequest;
import com.hopeandsparks.auth.dto.UserSettingsUpdateRequest;
import com.hopeandsparks.auth.entity.UserAccount;
import com.hopeandsparks.auth.entity.UserDeviceSession;
import com.hopeandsparks.auth.entity.UserLoginSession;
import com.hopeandsparks.auth.entity.UserProfileBasics;
import com.hopeandsparks.auth.entity.UserSettings;
import com.hopeandsparks.auth.repository.UserAccountRepository;
import com.hopeandsparks.auth.service.UserAuthService;
import com.hopeandsparks.auth.vo.PasswordResetTokenVO;
import com.hopeandsparks.auth.vo.UserAuthTokenVO;
import com.hopeandsparks.auth.vo.UserDeviceVO;
import com.hopeandsparks.auth.vo.UserProfileVO;
import com.hopeandsparks.auth.vo.UserSettingsVO;
import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.infra.redis.AuthTokenRedisStore;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.infra.security.JwtSubject;
import com.hopeandsparks.infra.security.JwtTokenService;
import com.hopeandsparks.infra.security.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_BANNED = 2;

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthTokenRedisStore tokenRedisStore;
    private final SecurityProperties securityProperties;
    private final Map<String, ResetTicket> resetTickets = new ConcurrentHashMap<>();

    /**
     * 装配前台认证所需的账号仓储、密码编码器、JWT 服务和 Redis 登录态存储。
     * 这个实现把用户表、会话表和 Redis token 串成一条完整的认证链路。
     */
    public UserAuthServiceImpl(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            AuthTokenRedisStore tokenRedisStore,
            SecurityProperties securityProperties
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.tokenRedisStore = tokenRedisStore;
        this.securityProperties = securityProperties;
    }

    /**
     * 注册前台用户并自动登录。
     * 会先校验用户名、手机号、邮箱唯一性，再写入 sys_user、默认 user_settings 和 user_login_session，
     * 最后签发 access token 并把登录态保存到 Redis。
     */
    @Override
    @Transactional
    public UserAuthTokenVO register(UserRegisterRequest request, HttpServletRequest servletRequest) {
        ensureUnique(request);
        Long userId = userAccountRepository.insertUser(request, passwordEncoder.encode(request.password()));
        userAccountRepository.insertDefaultSettings(userId);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(500, "用户创建后读取失败"));
        return issueToken(user, request.deviceId(), request.deviceName(), request.clientType(), clientIp(servletRequest));
    }

    /**
     * 使用用户名和密码登录前台账号。
     * 会校验账号存在、账号状态可登录以及 BCrypt 密码匹配，成功后创建新的登录会话并返回 JWT。
     */
    @Override
    @Transactional
    public UserAuthTokenVO login(UserLoginRequest request, HttpServletRequest servletRequest) {
        UserAccount user = userAccountRepository.findByAccount(request.account())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        ensureUserCanLogin(user);
        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return issueToken(user, request.deviceId(), request.deviceName(), request.clientType(), clientIp(servletRequest));
    }

    /**
     * 根据 sessionToken 刷新前台 access token。
     * 会确认 user_login_session 仍有效，必要时删除旧 Redis token，再签发新的 JWT 并刷新会话活跃时间。
     */
    @Override
    @Transactional
    public UserAuthTokenVO refresh(UserRefreshRequest request, String currentAccessToken) {
        UserLoginSession session = userAccountRepository.findActiveSession(request.sessionToken())
                .orElseThrow(() -> new BusinessException(401, "登录会话已失效"));
        if (session.expiresAt().isBefore(LocalDateTime.now())) {
            userAccountRepository.invalidateSession(session.sessionToken());
            throw new BusinessException(401, "登录会话已过期");
        }
        UserAccount user = userAccountRepository.findById(session.userId())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        ensureUserCanLogin(user);
        if (currentAccessToken != null && !currentAccessToken.isBlank()) {
            tokenRedisStore.delete(currentAccessToken, IdentityType.USER);
        }
        userAccountRepository.touchSession(session.sessionToken());
        return issueAccessToken(user, session.sessionToken());
    }

    /**
     * 退出前台登录。
     * 删除当前 access token 对应的 Redis 登录态；如果请求体里带了 sessionToken，也会同步把数据库会话置为失效。
     */
    @Override
    @Transactional
    public void logout(UserLogoutRequest request, String currentAccessToken) {
        if (currentAccessToken != null && !currentAccessToken.isBlank()) {
            tokenRedisStore.delete(currentAccessToken, IdentityType.USER);
        }
        if (request != null) {
            userAccountRepository.invalidateSession(request.sessionToken());
        }
    }

    /**
     * 获取当前登录前台用户的基础资料。
     * 只接受 USER 类型的安全主体，避免后台管理员 token 误用到前台用户接口。
     */
    @Override
    public UserProfileVO currentUser(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            throw new BusinessException(401, "请先登录前台账号");
        }
        UserAccount user = userAccountRepository.findById(principal.id())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        return toProfile(user);
    }

    /**
     * W2 阶段先生成内存里的重置凭证，真实邮箱服务接入后再改成邮件发送。
     */
    @Override
    public PasswordResetTokenVO requestPasswordReset(PasswordResetRequest request) {
        UserAccount user = userAccountRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(404, "邮箱未绑定前台账号"));
        String resetToken = UUID.randomUUID().toString().replace("-", "");
        resetTickets.put(resetToken, new ResetTicket(user.id(), LocalDateTime.now().plusMinutes(15)));
        return new PasswordResetTokenVO(resetToken, 15 * 60);
    }

    /**
     * 使用找回密码凭证重置密码，并让数据库中的登录会话失效。
     */
    @Override
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        ResetTicket ticket = resetTickets.remove(request.resetToken());
        if (ticket == null || ticket.expiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "重置凭证无效或已过期");
        }
        userAccountRepository.updatePassword(ticket.userId(), passwordEncoder.encode(request.newPassword()));
        userAccountRepository.invalidateUserSessions(ticket.userId());
    }

    @Override
    public List<UserDeviceVO> listDevices(AuthenticatedPrincipal principal) {
        Long userId = requireUserId(principal);
        String currentSessionToken = principal.sessionToken();
        return userAccountRepository.listActiveSessions(userId).stream()
                .map(session -> toDeviceVO(session, currentSessionToken))
                .toList();
    }

    @Override
    @Transactional
    public void offlineDevice(AuthenticatedPrincipal principal, String sessionId) {
        Long userId = requireUserId(principal);
        Long parsedSessionId = parseId(sessionId, "登录会话ID格式不正确");
        boolean updated = userAccountRepository.invalidateSessionById(userId, parsedSessionId);
        if (!updated) {
            throw new BusinessException(404, "登录会话不存在");
        }
    }

    @Override
    @Transactional
    public void changePassword(AuthenticatedPrincipal principal, UserChangePasswordRequest request) {
        Long userId = requireUserId(principal);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (!passwordEncoder.matches(request.oldPassword(), user.passwordHash())) {
            throw new BusinessException(400, "旧密码不正确");
        }
        userAccountRepository.updatePassword(userId, passwordEncoder.encode(request.newPassword()));
        userAccountRepository.invalidateUserSessions(userId);
    }

    @Override
    @Transactional
    public void changeEmail(AuthenticatedPrincipal principal, UserChangeEmailRequest request) {
        Long userId = requireUserId(principal);
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (userAccountRepository.existsByEmailForOtherUser(request.email(), userId)) {
            throw new BusinessException(409, "邮箱已绑定其他账号");
        }
        if (request.password() != null && !request.password().isBlank()
                && !passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new BusinessException(400, "密码不正确");
        }
        userAccountRepository.updateEmail(userId, request.email());
    }

    @Override
    public UserSettingsVO getSettings(AuthenticatedPrincipal principal) {
        Long userId = requireUserId(principal);
        UserSettings settings = userAccountRepository.findSettings(userId)
                .orElseGet(() -> {
                    userAccountRepository.insertDefaultSettings(userId);
                    return userAccountRepository.findSettings(userId)
                            .orElseThrow(() -> new BusinessException(500, "用户设置初始化失败"));
                });
        return toSettingsVO(settings);
    }

    @Override
    @Transactional
    public UserSettingsVO updateSettings(AuthenticatedPrincipal principal, UserSettingsUpdateRequest request) {
        Long userId = requireUserId(principal);
        if (userAccountRepository.findSettings(userId).isEmpty()) {
            userAccountRepository.insertDefaultSettings(userId);
        }
        userAccountRepository.updateSettings(
                userId,
                firstNonNull(request.enableTts(), boolFromMap(request.notification(), "tts")),
                firstNonNull(request.enableAvaPopup(), avaPopupFromRequest(request)),
                firstNonNull(request.enableFocusMode(), boolFromMap(request.learningPreference(), "focusMode")),
                firstNonNull(request.publicCollection(), boolFromMap(request.privacy(), "profileVisible")),
                firstText(request.themeMode(), textFromMap(request.theme(), "mode")),
                firstText(request.fontScale(), textFromMap(request.learningPreference(), "fontScale"))
        );
        return getSettings(principal);
    }

    @Override
    public void clearCache(AuthenticatedPrincipal principal) {
        requireUserId(principal);
        // W2 暂时没有用户本地缓存表，接口保留为成功，便于前端触发清理动作。
    }

    /**
     * 校验注册请求中的账号标识没有被占用。
     * 用户名必查，手机号和邮箱为空时跳过，避免唯一索引字段被无意义占用。
     */
    private void ensureUnique(UserRegisterRequest request) {
        if (userAccountRepository.existsByUsername(request.username())) {
            throw new BusinessException(409, "用户名已存在");
        }
        if (userAccountRepository.existsByPhone(request.phone())) {
            throw new BusinessException(409, "手机号已绑定");
        }
        if (userAccountRepository.existsByEmail(request.email())) {
            throw new BusinessException(409, "邮箱已绑定");
        }
    }

    /**
     * 校验用户账号是否允许登录。
     * 会拦截封禁、注销等非正常状态，并对存在封禁截止时间的账号做时间判断。
     */
    private void ensureUserCanLogin(UserAccount user) {
        if (user.accountStatus() == STATUS_BANNED) {
            throw new BusinessException(403, user.banReason() == null ? "账号已被封禁" : user.banReason());
        }
        if (user.accountStatus() != STATUS_NORMAL) {
            throw new BusinessException(403, "账号状态不可登录");
        }
        if (user.banUntil() != null && user.banUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(403, "账号封禁中");
        }
    }

    /**
     * 创建一条新的用户登录会话并签发 access token。
     * sessionToken 存入 user_login_session，用于后续刷新 token 和设备会话治理。
     */
    private UserAuthTokenVO issueToken(UserAccount user, String deviceId, String deviceName, String clientType, String ipAddress) {
        String sessionToken = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plus(securityProperties.getJwt().getUserExpiration());
        userAccountRepository.insertSession(user.id(), sessionToken, deviceId, deviceName, clientType, ipAddress, expiresAt);
        return issueAccessToken(user, sessionToken);
    }

    /**
     * 为指定用户和会话签发前台 JWT。
     * JWT 中携带用户身份和 sessionToken，同时按 auth:user:token:{token} 写入 Redis 以支持服务端失效。
     */
    private UserAuthTokenVO issueAccessToken(UserAccount user, String sessionToken) {
        JwtSubject subject = new JwtSubject(user.id(), user.username(), IdentityType.USER, sessionToken);
        String accessToken = jwtTokenService.generateToken(subject, securityProperties.getJwt().getUserExpiration());
        tokenRedisStore.save(accessToken, subject, securityProperties.getJwt().getUserExpiration());
        return new UserAuthTokenVO(
                accessToken,
                securityProperties.getJwt().getTokenPrefix(),
                securityProperties.getJwt().getUserExpiration().toSeconds(),
                sessionToken,
                toProfile(user)
        );
    }

    /**
     * 把数据库用户账号转换成前端展示用的基础资料对象。
     * 这里不暴露 passwordHash、状态和封禁信息，避免敏感字段进入响应体。
     */
    private UserProfileVO toProfile(UserAccount user) {
        UserProfileBasics profile = userAccountRepository.findProfileBasics(user.id()).orElse(null);
        return new UserProfileVO(
                String.valueOf(user.id()),
                user.username(),
                user.nickname(),
                user.avatarUrl(),
                user.phone(),
                user.email(),
                profile != null,
                profile == null ? null : profile.learningGoal(),
                profile == null ? null : profile.majorDomain(),
                profile == null ? null : profile.gradeLevel(),
                profile == null ? null : profile.knowledgeBaseLevel(),
                profile == null ? null : profile.selfDiscipline()
        );
    }

    private UserDeviceVO toDeviceVO(UserDeviceSession session, String currentSessionToken) {
        return new UserDeviceVO(
                String.valueOf(session.id()),
                session.deviceId(),
                session.deviceName(),
                session.clientType(),
                session.ipAddress(),
                session.lastActiveAt(),
                session.expiresAt(),
                session.sessionToken() != null && session.sessionToken().equals(currentSessionToken)
        );
    }

    private UserSettingsVO toSettingsVO(UserSettings settings) {
        return new UserSettingsVO(
                Map.of("language", "zh-CN", "autoStart", true),
                Map.of(
                        "sageGuideLevel", 8,
                        "coachPressureMode", settings.enableFocusMode(),
                        "avaInterventionFrequency", settings.enableAvaPopup() ? "medium" : "off"
                ),
                Map.of("mode", nullToDefault(settings.themeMode(), "light"), "animation", true),
                Map.of(
                        "agent", settings.enableAvaPopup(),
                        "community", true,
                        "system", true,
                        "tts", settings.enableTts()
                ),
                Map.of(
                        "profileVisible", settings.publicCollection(),
                        "learningStatsVisible", false
                ),
                Map.of(
                        "focusMode", settings.enableFocusMode(),
                        "fontScale", nullToDefault(settings.fontScale(), "normal")
                )
        );
    }

    private Long requireUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            throw new BusinessException(401, "请先登录前台账号");
        }
        return principal.id();
    }

    private Long parseId(String id, String message) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException exception) {
            throw new BusinessException(400, message);
        }
    }

    private Boolean avaPopupFromRequest(UserSettingsUpdateRequest request) {
        Object frequency = request.agent() == null ? null : request.agent().get("avaInterventionFrequency");
        if (frequency instanceof String text) {
            return !"off".equalsIgnoreCase(text);
        }
        return boolFromMap(request.agent(), "enableAvaPopup");
    }

    private Boolean boolFromMap(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text);
        }
        return null;
    }

    private String textFromMap(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Boolean firstNonNull(Boolean first, Boolean second) {
        return first != null ? first : second;
    }

    private String firstText(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private String nullToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * 提取客户端真实 IP。
     * 优先使用网关或反向代理传入的 X-Forwarded-For，缺失时退回 Servlet 容器记录的远端地址。
     */
    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record ResetTicket(Long userId, LocalDateTime expiresAt) {
    }
}
