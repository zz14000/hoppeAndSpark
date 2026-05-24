package com.hopeandsparks.auth.repository;

import com.hopeandsparks.auth.dto.UserRegisterRequest;
import com.hopeandsparks.auth.entity.UserAccount;
import com.hopeandsparks.auth.entity.UserLoginSession;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserAccountRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<UserAccount> userMapper = (rs, rowNum) -> new UserAccount(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("nickname"),
            rs.getString("password_hash"),
            rs.getString("avatar_url"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getInt("account_status"),
            rs.getString("ban_reason"),
            rs.getTimestamp("ban_until") == null ? null : rs.getTimestamp("ban_until").toLocalDateTime()
    );

    private final RowMapper<UserLoginSession> sessionMapper = (rs, rowNum) -> new UserLoginSession(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("session_token"),
            rs.getTimestamp("expires_at").toLocalDateTime(),
            rs.getInt("session_status")
    );

    public UserAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserAccount> findByUsername(String username) {
        return queryUser("""
                select id, username, nickname, password_hash, avatar_url, phone, email,
                       account_status, ban_reason, ban_until
                from sys_user
                where username = ? and is_deleted = 0
                """, username);
    }

    public Optional<UserAccount> findById(Long id) {
        return queryUser("""
                select id, username, nickname, password_hash, avatar_url, phone, email,
                       account_status, ban_reason, ban_until
                from sys_user
                where id = ? and is_deleted = 0
                """, id);
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from sys_user where username = ? and is_deleted = 0",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from sys_user where phone = ? and is_deleted = 0",
                Integer.class,
                phone
        );
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from sys_user where email = ? and is_deleted = 0",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    public Long insertUser(UserRegisterRequest request, String passwordHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into sys_user(username, nickname, password_hash, phone, email, account_status)
                    values (?, ?, ?, ?, ?, 1)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.username());
            ps.setString(2, blankToNull(request.nickname()));
            ps.setString(3, passwordHash);
            ps.setString(4, blankToNull(request.phone()));
            ps.setString(5, blankToNull(request.email()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public void insertDefaultSettings(Long userId) {
        jdbcTemplate.update("""
                insert into user_settings(user_id, enable_tts, enable_ava_popup, enable_focus_mode,
                                          public_collection, theme_mode, font_scale)
                values (?, 1, 1, 1, 1, 'light', 'normal')
                """, userId);
    }

    public Long insertSession(
            Long userId,
            String sessionToken,
            String deviceId,
            String deviceName,
            String clientType,
            String ipAddress,
            LocalDateTime expiresAt
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into user_login_session(user_id, session_token, device_id, device_name,
                                                   client_type, ip_address, last_active_at, expires_at, session_status)
                    values (?, ?, ?, ?, ?, ?, now(), ?, 1)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, sessionToken);
            ps.setString(3, blankToNull(deviceId));
            ps.setString(4, blankToNull(deviceName));
            ps.setString(5, blankToNull(clientType));
            ps.setString(6, blankToNull(ipAddress));
            ps.setObject(7, expiresAt);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public Optional<UserLoginSession> findActiveSession(String sessionToken) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, user_id, session_token, expires_at, session_status
                    from user_login_session
                    where session_token = ? and session_status = 1 and is_deleted = 0
                    """, sessionMapper, sessionToken));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public void touchSession(String sessionToken) {
        jdbcTemplate.update("""
                update user_login_session
                set last_active_at = now()
                where session_token = ? and session_status = 1 and is_deleted = 0
                """, sessionToken);
    }

    public void invalidateSession(String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) {
            return;
        }
        jdbcTemplate.update("""
                update user_login_session
                set session_status = 0
                where session_token = ? and is_deleted = 0
                """, sessionToken);
    }

    private Optional<UserAccount> queryUser(String sql, Object... args) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userMapper, args));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
