package com.hopeandsparks.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InfraSecurityConfig {

    /**
     * 提供全局密码编码器。
     * 注册和登录都会使用 BCrypt，保证前台用户和后台管理员密码采用同一套不可逆哈希策略。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
