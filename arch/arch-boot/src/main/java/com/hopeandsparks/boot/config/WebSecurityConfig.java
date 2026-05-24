package com.hopeandsparks.boot.config;

import com.hopeandsparks.boot.security.ApiAccessDeniedHandler;
import com.hopeandsparks.boot.security.ApiAuthenticationEntryPoint;
import com.hopeandsparks.boot.security.JwtAuthenticationFilter;
import com.hopeandsparks.boot.security.ManageDynamicAuthorizationFilter;
import com.hopeandsparks.infra.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class WebSecurityConfig {

    private final SecurityProperties securityProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ManageDynamicAuthorizationFilter manageDynamicAuthorizationFilter;
    private final ApiAuthenticationEntryPoint authenticationEntryPoint;
    private final ApiAccessDeniedHandler accessDeniedHandler;

    /**
     * 装配 Web 安全配置所需的过滤器和异常处理器。
     * 这里把 JWT 认证、后台动态授权和统一 JSON 错误响应集中接入 Spring Security。
     */
    public WebSecurityConfig(
            SecurityProperties securityProperties,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ManageDynamicAuthorizationFilter manageDynamicAuthorizationFilter,
            ApiAuthenticationEntryPoint authenticationEntryPoint,
            ApiAccessDeniedHandler accessDeniedHandler
    ) {
        this.securityProperties = securityProperties;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.manageDynamicAuthorizationFilter = manageDynamicAuthorizationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * 构建无状态 API 安全过滤链。
     * 白名单接口直接放行，Manage 后台接口要求 ADMIN 身份，其他 /api/v1 接口要求已登录。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] publicUrls = securityProperties.getPublicUrls().toArray(String[]::new);
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(publicUrls).permitAll()
                        .requestMatchers("/api/v1/manage/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(manageDynamicAuthorizationFilter, AuthorizationFilter.class);
        return http.build();
    }

    /**
     * 配置跨域访问策略。
     * 当前允许任意来源调用 API，暴露 X-Request-Id 方便前端联动日志，适合本地联调和 Swagger 在线测试。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-Request-Id"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
