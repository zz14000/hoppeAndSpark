package com.hopeandsparks.boot.security;

import com.hopeandsparks.infra.redis.AuthTokenRedisStore;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.JwtSubject;
import com.hopeandsparks.infra.security.JwtTokenService;
import com.hopeandsparks.infra.security.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AuthTokenRedisStore tokenRedisStore;
    private final SecurityProperties securityProperties;

    /**
     * 注入 JWT 解析服务、Redis 登录态存储和安全配置。
     * 过滤器依赖这些组件完成 token 提取、签名校验和服务端登录态校验。
     */
    public JwtAuthenticationFilter(
            JwtTokenService jwtTokenService,
            AuthTokenRedisStore tokenRedisStore,
            SecurityProperties securityProperties
    ) {
        this.jwtTokenService = jwtTokenService;
        this.tokenRedisStore = tokenRedisStore;
        this.securityProperties = securityProperties;
    }

    /**
     * 在每个请求进入 Controller 前尝试恢复认证上下文。
     * 如果 Authorization 中存在有效 JWT 且 Redis 登录态未失效，就把用户或管理员身份写入 SecurityContext。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(token);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 解析并接受一个 access token。
     * 该方法会校验 JWT 自身是否合法，再检查 Redis 中是否还存在对应登录态，双重通过后才建立 Authentication。
     */
    private void authenticate(String token) {
        try {
            JwtSubject subject = jwtTokenService.parse(token);
            if (!tokenRedisStore.exists(token, subject.type())) {
                return;
            }
            AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                    subject.id(),
                    subject.username(),
                    subject.type(),
                    subject.sessionToken()
            );
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    token,
                    List.of(new SimpleGrantedAuthority(subject.type().name()))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 从 Authorization 请求头中提取裸 JWT。
     * 兼容标准 Bearer 空格格式，也兼容旧项目里可能出现的 Bearer 直接拼接 token 格式。
     */
    private String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String prefix = securityProperties.getJwt().getTokenPrefix();
        if (authorization.startsWith(prefix + " ")) {
            return authorization.substring(prefix.length() + 1);
        }
        if (authorization.startsWith(prefix)) {
            return authorization.substring(prefix.length());
        }
        return authorization;
    }
}
