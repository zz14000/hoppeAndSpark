package com.hopeandsparks.infra.redis;

import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.infra.security.JwtSubject;
import com.hopeandsparks.infra.security.SecurityProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthTokenRedisStore {

    private final StringRedisTemplate redisTemplate;
    private final SecurityProperties securityProperties;

    /**
     * 注入 Redis 字符串模板和安全配置。
     * 安全配置提供前台与后台 token key 前缀，保证 Redis key 和数据库设计文档保持一致。
     */
    public AuthTokenRedisStore(StringRedisTemplate redisTemplate, SecurityProperties securityProperties) {
        this.redisTemplate = redisTemplate;
        this.securityProperties = securityProperties;
    }

    /**
     * 保存 access token 对应的服务端登录态。
     * 前台 token 写入 auth:user:token:{token}，后台 token 写入 auth:admin:token:{token}，并设置和 JWT 一致的 TTL。
     */
    public void save(String token, JwtSubject subject, Duration ttl) {
        redisTemplate.opsForValue().set(keyFor(token, subject.type()), valueFor(subject), ttl);
    }

    /**
     * 判断 token 的 Redis 登录态是否仍然存在。
     * JWT 过滤器会用这个结果支持主动登出、强制下线和服务端 token 失效。
     */
    public boolean exists(String token, IdentityType type) {
        Boolean result = redisTemplate.hasKey(keyFor(token, type));
        return Boolean.TRUE.equals(result);
    }

    /**
     * 删除指定 token 的 Redis 登录态。
     * 删除后即使 JWT 本身还没过期，请求过滤器也不会再接受该 token。
     */
    public void delete(String token, IdentityType type) {
        redisTemplate.delete(keyFor(token, type));
    }

    /**
     * 按身份类型构造 Redis token key。
     * Key 格式严格使用数据库设计中的 auth:user:token:{token} 和 auth:admin:token:{token}。
     */
    public String keyFor(String token, IdentityType type) {
        String prefix = type == IdentityType.ADMIN
                ? securityProperties.getRedis().getAdminTokenPrefix()
                : securityProperties.getRedis().getUserTokenPrefix();
        return prefix + ":" + token;
    }

    /**
     * 生成 Redis 登录态值。
     * 前台保存 userId/sessionToken 便于追踪具体设备会话，后台保存 adminId 即可定位管理员身份。
     */
    private String valueFor(JwtSubject subject) {
        if (subject.type() == IdentityType.USER) {
            return subject.id() + "/" + nullToEmpty(subject.sessionToken());
        }
        return String.valueOf(subject.id());
    }

    /**
     * 把可能为空的字符串转换为空串。
     * 主要用于后台 token 没有 sessionToken 时，避免 Redis value 出现 null 文本。
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
