package com.hopeandsparks.infra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {

    private final SecurityProperties securityProperties;

    /**
     * 注入安全配置，用于读取 JWT 密钥、过期时间和请求头约定。
     * 密钥会在签发和解析 token 时统一使用，保证前后台身份校验逻辑一致。
     */
    public JwtTokenService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * 根据身份主体和有效期生成 JWT。
     * token payload 中写入用户名、用户或管理员 ID、身份类型和可选 sessionToken，供请求过滤器恢复登录上下文。
     */
    public String generateToken(JwtSubject subject, Duration ttl) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);
        return Jwts.builder()
                .subject(subject.username())
                .claim("id", subject.id())
                .claim("type", subject.type().name())
                .claim("session", subject.sessionToken())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    /**
     * 解析并校验 JWT，返回系统内部统一使用的身份主体。
     * 如果签名无效、token 过期或必要字段缺失，会抛出 InvalidTokenException 交给认证过滤器处理。
     */
    public JwtSubject parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object rawId = claims.get("id");
            Long id = rawId instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(rawId));
            IdentityType type = IdentityType.valueOf(String.valueOf(claims.get("type")));
            String sessionToken = claims.get("session", String.class);
            return new JwtSubject(id, claims.getSubject(), type, sessionToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidTokenException("Invalid or expired token", exception);
        }
    }

    /**
     * 构造 JJWT 所需的 HMAC 签名密钥。
     * 配置的密钥长度不足时会先做 SHA-256 摘要，避免弱长度密钥导致运行期异常。
     */
    private SecretKey signingKey() {
        byte[] keyBytes = securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = sha256(keyBytes);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 对原始密钥字节做 SHA-256 摘要。
     * 该方法只用于补足开发环境短密钥的签名长度，不改变外部 token payload。
     */
    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
