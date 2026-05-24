package com.hopeandsparks.manage.repository;

import com.hopeandsparks.manage.dto.AdminRegisterRequest;
import com.hopeandsparks.manage.entity.AdminAccount;
import com.hopeandsparks.manage.entity.AdminMenu;
import com.hopeandsparks.manage.entity.AdminResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminAuthRepository {

    private static final String SUPER_ADMIN_ROLE_KEY = "super_admin";

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AdminAccount> adminMapper = (rs, rowNum) -> new AdminAccount(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("real_name"),
            rs.getString("password_hash"),
            rs.getInt("admin_status")
    );

    private final RowMapper<AdminResource> resourceMapper = (rs, rowNum) -> new AdminResource(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("code"),
            rs.getString("url")
    );

    private final RowMapper<AdminMenu> menuMapper = (rs, rowNum) -> new AdminMenu(
            rs.getLong("id"),
            rs.getLong("parent_id"),
            rs.getString("name"),
            rs.getString("path"),
            rs.getInt("leval"),
            rs.getInt("sort_order")
    );

    public AdminAuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countAdmins() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from sys_admin where is_deleted = 0",
                Integer.class
        );
        return count == null ? 0 : count;
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from sys_admin where username = ? and is_deleted = 0",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }

    public Optional<AdminAccount> findByUsername(String username) {
        return queryAdmin("""
                select id, username, real_name, password_hash, admin_status
                from sys_admin
                where username = ? and is_deleted = 0
                """, username);
    }

    public Optional<AdminAccount> findById(Long id) {
        return queryAdmin("""
                select id, username, real_name, password_hash, admin_status
                from sys_admin
                where id = ? and is_deleted = 0
                """, id);
    }

    public Long insertAdmin(AdminRegisterRequest request, String passwordHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into sys_admin(username, real_name, password_hash, admin_status)
                    values (?, ?, ?, 1)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.username());
            ps.setString(2, request.realName());
            ps.setString(3, passwordHash);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public void grantSuperAdmin(Long adminId) {
        Long roleId = findRoleId(SUPER_ADMIN_ROLE_KEY).orElseGet(this::createSuperAdminRole);
        jdbcTemplate.update("insert ignore into sys_admin_role(admin_id, role_id) values (?, ?)", adminId, roleId);
        jdbcTemplate.update("""
                insert ignore into sys_role_admin_menu(role_id, menu_id)
                select ?, id
                from sys_admin_menu
                where status = 1 and is_deleted = 0
                """, roleId);
        jdbcTemplate.update("""
                insert ignore into sys_role_admin_resource(role_id, resource_id)
                select ?, id
                from sys_admin_resource
                where status = 1 and is_deleted = 0
                """, roleId);
    }

    public List<String> listRoleKeys(Long adminId) {
        return jdbcTemplate.queryForList("""
                select distinct r.role_key
                from sys_admin_role ar
                join sys_role r on r.id = ar.role_id
                where ar.admin_id = ? and r.status = 1
                order by r.id
                """, String.class, adminId);
    }

    public List<AdminMenu> listMenus(Long adminId) {
        return jdbcTemplate.query("""
                select distinct m.id, m.parent_id, m.name, m.path, m.leval, m.sort_order
                from sys_admin_role ar
                join sys_role r on r.id = ar.role_id and r.status = 1
                join sys_role_admin_menu rm on rm.role_id = r.id
                join sys_admin_menu m on m.id = rm.menu_id
                where ar.admin_id = ? and m.status = 1 and m.is_deleted = 0
                order by m.leval, m.sort_order, m.id
                """, menuMapper, adminId);
    }

    public List<AdminResource> listGrantedResources(Long adminId) {
        return jdbcTemplate.query("""
                select distinct res.id, res.name, res.code, res.url
                from sys_admin_role ar
                join sys_role r on r.id = ar.role_id and r.status = 1
                join sys_role_admin_resource rr on rr.role_id = r.id
                join sys_admin_resource res on res.id = rr.resource_id
                where ar.admin_id = ? and res.status = 1 and res.is_deleted = 0
                order by res.sort_order, res.id
                """, resourceMapper, adminId);
    }

    public List<AdminResource> listAllEnabledResources() {
        return jdbcTemplate.query("""
                select id, name, code, url
                from sys_admin_resource
                where status = 1 and is_deleted = 0
                order by sort_order, id
                """, resourceMapper);
    }

    private Optional<Long> findRoleId(String roleKey) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "select id from sys_role where role_key = ? and status = 1",
                    Long.class,
                    roleKey
            ));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private Long createSuperAdminRole() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into sys_role(role_name, role_key, status)
                    values ('超级管理员', ?, 1)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, SUPER_ADMIN_ROLE_KEY);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    private Optional<AdminAccount> queryAdmin(String sql, Object... args) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, adminMapper, args));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
