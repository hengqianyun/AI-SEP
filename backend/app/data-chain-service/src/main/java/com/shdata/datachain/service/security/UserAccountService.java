package com.shdata.datachain.service.security;

import com.shdata.datachain.common.exception.BusinessException;
import com.shdata.datachain.common.security.PasswordHasher;
import com.shdata.datachain.entity.SysUserEntity;
import com.shdata.datachain.model.AdminUser;
import com.shdata.datachain.model.Role;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.repository.SysUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 持久化用户账号服务（权威登录源）。
 *
 * <p>演示种子：admin / provider / user，密码均为 {@value #DEFAULT_DEMO_PASSWORD}（BCrypt 存储，不落明文生产密钥）。
 */
@Service
public class UserAccountService implements ApplicationRunner {

    /** 文档化演示口令；仅用于本地/演示种子，不得作为生产密钥明文落盘。 */
    public static final String DEFAULT_DEMO_PASSWORD = "demo";

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SysUserRepository userRepo;
    private final PasswordHasher passwordHasher;

    public UserAccountService(SysUserRepository userRepo, PasswordHasher passwordHasher) {
        this.userRepo = userRepo;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureSeedUsers();
    }

    @Transactional
    public void ensureSeedUsers() {
        if (!userRepo.findByDelFlagOrderByIdAsc(false).isEmpty()) {
            return;
        }
        seed("admin", "演示管理员", Role.ADMIN);
        seed("provider", "演示提供方", Role.PROVIDER);
        seed("user", "演示普通用户", Role.USER);
    }

    private void seed(String username, String displayName, Role role) {
        SysUserEntity e = new SysUserEntity();
        e.setUsername(username);
        e.setPasswordHash(passwordHasher.hash(DEFAULT_DEMO_PASSWORD));
        e.setDisplayName(displayName);
        e.setRole(role.name());
        e.setEnterpriseName("演示企业");
        e.setCreateBy("system");
        e.setUpdateBy("system");
        userRepo.save(e);
    }

    /**
     * 认证：用户名不区分大小写；角色取自用户记录。
     */
    @Transactional(readOnly = true)
    public Optional<SessionPrincipal> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        Optional<SysUserEntity> found =
                userRepo.findByUsernameAndDelFlag(username.trim().toLowerCase(), false);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        SysUserEntity user = found.get();
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            return Optional.empty();
        }
        Role role = Role.fromString(user.getRole());
        return Optional.of(
                new SessionPrincipal(
                        String.valueOf(user.getId()),
                        user.getDisplayName(),
                        role,
                        user.getEnterpriseName()));
    }

    @Transactional(readOnly = true)
    public List<AdminUser> listUsers() {
        List<AdminUser> items = new ArrayList<>();
        // 管理端需展示已停用账号，便于「启用」恢复；登录仍只认 delFlag=false。
        for (SysUserEntity u : userRepo.findAllByOrderByIdAsc()) {
            items.add(toView(u));
        }
        return items;
    }

    @Transactional
    public AdminUser createUser(
            String username,
            String password,
            String displayName,
            Role role,
            String enterpriseName,
            String actorUserId) {
        if (username == null || username.isBlank()) {
            throw new BusinessException(400, "ERR_VALIDATION", "username 必填", null);
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(400, "ERR_VALIDATION", "password 必填", null);
        }
        if (role == null) {
            throw new BusinessException(400, "ERR_VALIDATION", "role 必填", null);
        }
        String uname = username.trim().toLowerCase();
        if (userRepo.existsByUsername(uname)) {
            throw new BusinessException(409, "ERR_USER_USERNAME_CONFLICT", "用户名已存在", null);
        }
        SysUserEntity e = new SysUserEntity();
        e.setUsername(uname);
        e.setPasswordHash(passwordHasher.hash(password));
        e.setDisplayName(displayName == null || displayName.isBlank() ? uname : displayName.trim());
        e.setRole(role.name());
        e.setEnterpriseName(enterpriseName == null ? "" : enterpriseName.trim());
        e.setCreateBy(actorUserId == null ? "" : actorUserId);
        e.setUpdateBy(actorUserId == null ? "" : actorUserId);
        return toView(userRepo.save(e));
    }

    @Transactional
    public AdminUser updateUser(
            long userId,
            String displayName,
            Role role,
            String enterpriseName,
            String password,
            Boolean deleted,
            String actorUserId) {
        SysUserEntity e =
                userRepo
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                404, "ERR_USER_NOT_FOUND", "用户不存在", null));
        if (Boolean.TRUE.equals(e.getDelFlag()) && !Boolean.FALSE.equals(deleted)) {
            throw new BusinessException(404, "ERR_USER_NOT_FOUND", "用户不存在", null);
        }
        if (displayName != null) {
            e.setDisplayName(displayName.trim());
        }
        if (role != null) {
            e.setRole(role.name());
        }
        if (enterpriseName != null) {
            e.setEnterpriseName(enterpriseName.trim());
        }
        if (password != null && !password.isBlank()) {
            e.setPasswordHash(passwordHasher.hash(password));
        }
        if (Boolean.TRUE.equals(deleted)) {
            e.setDelFlag(true);
        } else if (Boolean.FALSE.equals(deleted)) {
            e.setDelFlag(false);
        }
        e.setUpdateBy(actorUserId == null ? "" : actorUserId);
        return toView(userRepo.save(e));
    }

    /**
     * 物理删除用户行（硬删）。停用/启用仍走 {@link #updateUser} 的 deleted 软删。
     *
     * @param userId 目标用户主键
     * @param actorUserId 当前操作者会话 userId（字符串形式的数字 id）
     * @throws BusinessException 用户不存在（404）或禁止删除自身（400 ERR_USER_DELETE_SELF）
     */
    @Transactional
    public void deleteUser(long userId, String actorUserId) {
        if (String.valueOf(userId).equals(actorUserId)) {
            throw new BusinessException(400, "ERR_USER_DELETE_SELF", "不能删除当前登录账号", null);
        }
        SysUserEntity e =
                userRepo
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                404, "ERR_USER_NOT_FOUND", "用户不存在", null));
        userRepo.delete(e);
    }

    private static AdminUser toView(SysUserEntity u) {
        return new AdminUser(
                String.valueOf(u.getId()),
                u.getUsername(),
                u.getDisplayName(),
                Role.fromString(u.getRole()),
                u.getEnterpriseName() == null ? "" : u.getEnterpriseName(),
                Boolean.TRUE.equals(u.getDelFlag()),
                u.getCreateTime() == null ? null : ISO.format(u.getCreateTime()),
                u.getUpdateTime() == null ? null : ISO.format(u.getUpdateTime()));
    }
}
