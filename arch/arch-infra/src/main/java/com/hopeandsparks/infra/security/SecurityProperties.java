package com.hopeandsparks.infra.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "hope.security")
public class SecurityProperties {

    private final Jwt jwt = new Jwt();
    private final Redis redis = new Redis();
    private List<String> publicUrls = new ArrayList<>();

    /**
     * 读取 JWT 配置分组。
     * 该分组包含请求头、token 前缀、签名密钥以及前后台 token 过期时间。
     */
    public Jwt getJwt() {
        return jwt;
    }

    /**
     * 读取 Redis 登录态配置分组。
     * 该分组提供前台和后台 token 在 Redis 中使用的 key 前缀。
     */
    public Redis getRedis() {
        return redis;
    }

    /**
     * 读取无需认证即可访问的白名单路径。
     * Spring Security 会用这些 Ant 风格路径放行登录、注册、Swagger 和健康检查等接口。
     */
    public List<String> getPublicUrls() {
        return publicUrls;
    }

    /**
     * 绑定配置文件中的白名单路径。
     * Spring Boot 读取 hope.security.public-urls 后会调用该方法注入完整列表。
     */
    public void setPublicUrls(List<String> publicUrls) {
        this.publicUrls = publicUrls;
    }

    public static class Jwt {
        private String tokenHeader = "Authorization";
        private String tokenPrefix = "Bearer";
        private String secret = "hope-and-sparks-dev-secret-must-be-changed";
        private Duration userExpiration = Duration.ofDays(7);
        private Duration adminExpiration = Duration.ofHours(12);

        /**
         * 读取承载 JWT 的请求头名称。
         * 默认使用 Authorization，和 Swagger 的 Bearer 授权方式保持一致。
         */
        public String getTokenHeader() {
            return tokenHeader;
        }

        /**
         * 设置承载 JWT 的请求头名称。
         * 通常无需修改，除非网关或前端统一改用了其他自定义请求头。
         */
        public void setTokenHeader(String tokenHeader) {
            this.tokenHeader = tokenHeader;
        }

        /**
         * 读取 token 前缀。
         * 默认是 Bearer，过滤器会同时兼容 Bearer token 和 Bearertoken 两种拼接形式。
         */
        public String getTokenPrefix() {
            return tokenPrefix;
        }

        /**
         * 设置 token 前缀。
         * 该值会用于登录响应和 Authorization 请求头解析。
         */
        public void setTokenPrefix(String tokenPrefix) {
            this.tokenPrefix = tokenPrefix;
        }

        /**
         * 读取 JWT 签名密钥。
         * 生产环境应通过环境变量覆盖默认值，避免使用开发密钥签发真实 token。
         */
        public String getSecret() {
            return secret;
        }

        /**
         * 设置 JWT 签名密钥。
         * JwtTokenService 会基于该密钥构造 HMAC 签名 key。
         */
        public void setSecret(String secret) {
            this.secret = secret;
        }

        /**
         * 读取前台用户 token 有效期。
         * 当前默认 7 天，并同步用于 Redis 登录态 TTL。
         */
        public Duration getUserExpiration() {
            return userExpiration;
        }

        /**
         * 设置前台用户 token 有效期。
         * 支持 Spring Boot Duration 写法，例如 7d、12h、30m。
         */
        public void setUserExpiration(Duration userExpiration) {
            this.userExpiration = userExpiration;
        }

        /**
         * 读取后台管理员 token 有效期。
         * 当前默认 12 小时，并同步用于后台 Redis 登录态 TTL。
         */
        public Duration getAdminExpiration() {
            return adminExpiration;
        }

        /**
         * 设置后台管理员 token 有效期。
         * 后台权限敏感度更高，通常建议短于前台用户 token。
         */
        public void setAdminExpiration(Duration adminExpiration) {
            this.adminExpiration = adminExpiration;
        }
    }

    public static class Redis {
        private String userTokenPrefix = "auth:user:token";
        private String adminTokenPrefix = "auth:admin:token";

        /**
         * 读取前台用户登录态 Redis key 前缀。
         * 最终 key 会拼成 auth:user:token:{token}。
         */
        public String getUserTokenPrefix() {
            return userTokenPrefix;
        }

        /**
         * 设置前台用户登录态 Redis key 前缀。
         * 如需变更，必须和网关、运维脚本以及数据库设计口径同步。
         */
        public void setUserTokenPrefix(String userTokenPrefix) {
            this.userTokenPrefix = userTokenPrefix;
        }

        /**
         * 读取后台管理员登录态 Redis key 前缀。
         * 最终 key 会拼成 auth:admin:token:{token}。
         */
        public String getAdminTokenPrefix() {
            return adminTokenPrefix;
        }

        /**
         * 设置后台管理员登录态 Redis key 前缀。
         * 该配置用于区分前台用户 token 和后台管理员 token，避免身份域串用。
         */
        public void setAdminTokenPrefix(String adminTokenPrefix) {
            this.adminTokenPrefix = adminTokenPrefix;
        }
    }
}
