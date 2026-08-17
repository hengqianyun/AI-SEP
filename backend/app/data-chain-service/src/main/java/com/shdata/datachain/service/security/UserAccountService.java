package com.shdata.datachain.service.security;

import com.shdata.datachain.common.exception.BusinessException;
import com.shdata.datachain.common.security.PasswordHasher;
import com.shdata.datachain.entity.EnterpriseEntity;
import com.shdata.datachain.entity.SysUserEntity;
import com.shdata.datachain.model.AdminUser;
import com.shdata.datachain.model.Role;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.repository.EnterpriseRepository;
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
 * <p>Tips：
 * <ul>
 *   <li>{@link #authenticate} — 校验口令并组装含 enterpriseId/enterpriseName 的会话主体</li>
 *   <li>{@link #createUser} — ADMIN 创建用户，可指定或继承操作者企业</li>
 *   <li>{@link #backfillEnterpriseIds} — OQ-V16-002 orphan 回填到 UNASSIGNED，不得并入 DEMO</li>
 *   <li>{@link #ensureSeedUsers} — 演示种子：同一 DEMO 企业多名 ADMIN（admin + admin2）</li>
 * </ul>
 *
 * <p>演示种子：admin / admin2 / provider / user，密码均为 {@value #DEFAULT_DEMO_PASSWORD}（BCrypt 存储）。
 */
@Service
public class UserAccountService implements ApplicationRunner {

    /** 文档化演示口令；仅用于本地/演示种子，不得作为生产密钥明文落盘。 */
    public static final String DEFAULT_DEMO_PASSWORD = "demo";

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SysUserRepository userRepo;
    private final EnterpriseRepository enterpriseRepo;
    private final PasswordHasher passwordHasher;

    /**
     * 构造用户账号服务。
     *
     * @param userRepo 用户仓储
     * @param enterpriseRepo 企业仓储
     * @param passwordHasher 口令哈希
     */
    public UserAccountService(
            SysUserRepository userRepo,
            EnterpriseRepository enterpriseRepo,
            PasswordHasher passwordHasher) {
        this.userRepo = userRepo;
        this.enterpriseRepo = enterpriseRepo;
        this.passwordHasher = passwordHasher;
    }

    /**
     * 启动时确保企业种子、演示用户、以及历史用户 enterprise_id 回填。
     *
     * @param args 启动参数（未使用）
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureEnterprises();
        ensureSeedUsers();
        backfillEnterpriseIds();
    }

    /**
     * 确保 DEMO 与 UNASSIGNED 企业存在（幂等）。
     */
    @Transactional
    public void ensureEnterprises() {
        requireEnterprise(
                EnterpriseEntity.CODE_DEMO, EnterpriseEntity.NAME_DEMO, false);
        requireEnterprise(
                EnterpriseEntity.CODE_UNASSIGNED, EnterpriseEntity.NAME_UNASSIGNED, true);
    }

    /**
     * 空库时写入演示用户：同一 DEMO 企业含两名 ADMIN。
     */
    @Transactional
    public void ensureSeedUsers() {
        if (!userRepo.findByDelFlagOrderByIdAsc(false).isEmpty()) {
            return;
        }
        EnterpriseEntity demo = requireDemoEnterprise();
        seed("admin", "演示管理员", Role.ADMIN, demo);
        seed("admin2", "演示管理员二", Role.ADMIN, demo);
        seed("provider", "演示提供方", Role.PROVIDER, demo);
        seed("user", "演示普通用户", Role.USER, demo);
    }

    /**
     * 将 enterprise_id=0 的用户回填到名称匹配企业或 UNASSIGNED（OQ-V16-002）。
     *
     * <p>UNASSIGNED 与 DEMO 不同 id，orphan 不会因此获得演示企业产品写权限。
     */
    @Transactional
    public void backfillEnterpriseIds() {
        List<SysUserEntity> pending = userRepo.findByEnterpriseId(0L);
        if (pending.isEmpty()) {
            return;
        }
        EnterpriseEntity unassigned = requireUnassignedEnterprise();
        for (SysUserEntity user : pending) {
            EnterpriseEntity resolved = matchEnterpriseByName(user.getEnterpriseName())
                    .orElse(unassigned);
            applyEnterprise(user, resolved);
            userRepo.save(user);
        }
    }

    /**
     * 认证：用户名不区分大小写；角色与企业取自用户记录。
     *
     * @param username 登录名
     * @param password 明文口令
     * @return 成功则为含 enterpriseId 的会话主体
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
        return Optional.of(toPrincipal(user));
    }

    /**
     * 列出全部用户（含软删）。
     *
     * @return 管理端用户视图
     */
    @Transactional(readOnly = true)
    public List<AdminUser> listUsers() {
        List<AdminUser> items = new ArrayList<>();
        // 管理端需展示已停用账号，便于「启用」恢复；登录仍只认 delFlag=false。
        for (SysUserEntity u : userRepo.findAllByOrderByIdAsc()) {
            items.add(toView(u));
        }
        return items;
    }

    /**
     * 创建用户。未指定企业时继承操作者企业；不创建新企业。
     *
     * @param username 登录名
     * @param password 明文口令
     * @param displayName 显示名
     * @param role 角色
     * @param enterpriseId 可选企业 id（字符串）
     * @param enterpriseName 可选企业名（仅用于匹配已有企业）
     * @param actorUserId 操作者用户 id
     * @param actorEnterpriseId 操作者企业 id（继承默认）
     * @return 创建后的用户视图
     */
    @Transactional
    public AdminUser createUser(
            String username,
            String password,
            String displayName,
            Role role,
            String enterpriseId,
            String enterpriseName,
            String actorUserId,
            String actorEnterpriseId) {
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
        Long fallbackId = parseEnterpriseIdOrNull(actorEnterpriseId);
        EnterpriseEntity enterprise = resolveEnterprise(enterpriseId, enterpriseName, fallbackId);
        SysUserEntity e = new SysUserEntity();
        e.setUsername(uname);
        e.setPasswordHash(passwordHasher.hash(password));
        e.setDisplayName(displayName == null || displayName.isBlank() ? uname : displayName.trim());
        e.setRole(role.name());
        applyEnterprise(e, enterprise);
        e.setCreateBy(actorUserId == null ? "" : actorUserId);
        e.setUpdateBy(actorUserId == null ? "" : actorUserId);
        return toView(userRepo.save(e));
    }

    /**
     * 更新用户。enterpriseId 优先；仅企业名时匹配已有企业；均缺省则不改归属。
     *
     * @param userId 目标用户主键
     * @param displayName 显示名
     * @param role 角色
     * @param enterpriseId 可选企业 id
     * @param enterpriseName 可选企业名
     * @param password 可选新口令
     * @param deleted 软删/启用
     * @param actorUserId 操作者用户 id
     * @return 更新后的用户视图
     */
    @Transactional
    public AdminUser updateUser(
            long userId,
            String displayName,
            Role role,
            String enterpriseId,
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
        if ((enterpriseId != null && !enterpriseId.isBlank())
                || (enterpriseName != null && !enterpriseName.isBlank())) {
            EnterpriseEntity enterprise =
                    resolveEnterprise(enterpriseId, enterpriseName, e.getEnterpriseId());
            applyEnterprise(e, enterprise);
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

    /**
     * 按企业编码加载企业（测试与验收用）。
     *
     * @param code 企业编码
     * @return 未删除企业
     */
    @Transactional(readOnly = true)
    public Optional<EnterpriseEntity> findEnterpriseByCode(String code) {
        return enterpriseRepo.findByCodeAndDelFlag(code, false);
    }

    /**
     * 写入演示用户行并绑定企业。
     *
     * @param username 登录名
     * @param displayName 显示名
     * @param role 角色
     * @param enterprise 归属企业
     */
    private void seed(String username, String displayName, Role role, EnterpriseEntity enterprise) {
        SysUserEntity e = new SysUserEntity();
        e.setUsername(username);
        e.setPasswordHash(passwordHasher.hash(DEFAULT_DEMO_PASSWORD));
        e.setDisplayName(displayName);
        e.setRole(role.name());
        applyEnterprise(e, enterprise);
        e.setCreateBy("system");
        e.setUpdateBy("system");
        userRepo.save(e);
    }

    /**
     * 解析归属企业：id 优先，其次名称匹配已有企业，再否则 fallback；不新建企业。
     *
     * @param enterpriseId 请求企业 id
     * @param enterpriseName 请求企业名
     * @param fallbackId 创建时为操作者企业，更新时为原企业
     * @return 已存在企业
     */
    private EnterpriseEntity resolveEnterprise(
            String enterpriseId, String enterpriseName, Long fallbackId) {
        if (enterpriseId != null && !enterpriseId.isBlank()) {
            Long id = parseEnterpriseIdOrNull(enterpriseId);
            if (id == null || id <= 0) {
                throw new BusinessException(400, "ERR_VALIDATION", "enterpriseId 无效", null);
            }
            return enterpriseRepo
                    .findByIdAndDelFlag(id, false)
                    .orElseThrow(
                            () ->
                                    new BusinessException(
                                            400, "ERR_VALIDATION", "企业不存在", null));
        }
        if (enterpriseName != null && !enterpriseName.isBlank()) {
            Optional<EnterpriseEntity> byName = matchEnterpriseByName(enterpriseName);
            if (byName.isPresent()) {
                return byName.get();
            }
        }
        if (fallbackId != null && fallbackId > 0) {
            Optional<EnterpriseEntity> fallback =
                    enterpriseRepo.findByIdAndDelFlag(fallbackId, false);
            if (fallback.isPresent()) {
                return fallback.get();
            }
        }
        return requireUnassignedEnterprise();
    }

    /**
     * 按名称匹配已有企业：优先非 UNASSIGNED，再含占位。
     *
     * @param enterpriseName 企业名称
     * @return 匹配企业
     */
    private Optional<EnterpriseEntity> matchEnterpriseByName(String enterpriseName) {
        if (enterpriseName == null || enterpriseName.isBlank()) {
            return Optional.empty();
        }
        String name = enterpriseName.trim();
        Optional<EnterpriseEntity> named =
                enterpriseRepo.findFirstByNameAndDelFlagAndUnassignedDefaultOrderByIdAsc(
                        name, false, false);
        if (named.isPresent()) {
            return named;
        }
        return enterpriseRepo.findFirstByNameAndDelFlagOrderByIdAsc(name, false);
    }

    /**
     * 按编码获取或创建企业（种子幂等）。
     *
     * @param code 企业编码
     * @param name 企业名称
     * @param unassignedDefault 是否未归属占位
     * @return 持久化企业
     */
    private EnterpriseEntity requireEnterprise(String code, String name, boolean unassignedDefault) {
        return enterpriseRepo
                .findByCodeAndDelFlag(code, false)
                .orElseGet(
                        () -> {
                            EnterpriseEntity e = new EnterpriseEntity();
                            e.setCode(code);
                            e.setName(name);
                            e.setUnassignedDefault(unassignedDefault);
                            e.setCreateBy("system");
                            e.setUpdateBy("system");
                            return enterpriseRepo.save(e);
                        });
    }

    /**
     * 获取演示企业。
     *
     * @return DEMO 企业
     */
    private EnterpriseEntity requireDemoEnterprise() {
        return requireEnterprise(
                EnterpriseEntity.CODE_DEMO, EnterpriseEntity.NAME_DEMO, false);
    }

    /**
     * 获取未归属默认企业（OQ-V16-002 占位）。
     *
     * @return UNASSIGNED 企业
     */
    private EnterpriseEntity requireUnassignedEnterprise() {
        return enterpriseRepo
                .findFirstByUnassignedDefaultAndDelFlagOrderByIdAsc(true, false)
                .orElseGet(
                        () ->
                                requireEnterprise(
                                        EnterpriseEntity.CODE_UNASSIGNED,
                                        EnterpriseEntity.NAME_UNASSIGNED,
                                        true));
    }

    /**
     * 将企业 id 与规范名称同步到用户（enterprise_name 仅作反规范化缓存）。
     *
     * @param user 用户实体
     * @param enterprise 企业实体
     */
    private static void applyEnterprise(SysUserEntity user, EnterpriseEntity enterprise) {
        user.setEnterpriseId(enterprise.getId());
        user.setEnterpriseName(enterprise.getName());
    }

    /**
     * 组装会话主体；企业名优先取企业表。
     *
     * @param user 已认证用户
     * @return 会话主体
     */
    private SessionPrincipal toPrincipal(SysUserEntity user) {
        Role role = Role.fromString(user.getRole());
        String enterpriseId =
                user.getEnterpriseId() == null ? "" : String.valueOf(user.getEnterpriseId());
        String enterpriseName = canonicalEnterpriseName(user);
        return new SessionPrincipal(
                String.valueOf(user.getId()),
                user.getDisplayName(),
                role,
                enterpriseId,
                enterpriseName);
    }

    /**
     * 会话/列表展示用企业名：企业表为真源，缺失时回退反规范化列。
     *
     * @param user 用户实体
     * @return 企业名称
     */
    private String canonicalEnterpriseName(SysUserEntity user) {
        if (user.getEnterpriseId() != null && user.getEnterpriseId() > 0) {
            Optional<EnterpriseEntity> enterprise =
                    enterpriseRepo.findByIdAndDelFlag(user.getEnterpriseId(), false);
            if (enterprise.isPresent()) {
                return enterprise.get().getName();
            }
        }
        return user.getEnterpriseName() == null ? "" : user.getEnterpriseName();
    }

    /**
     * 转为管理端用户视图（含 enterpriseId）。
     *
     * @param u 用户实体
     * @return 响应模型
     */
    private AdminUser toView(SysUserEntity u) {
        String enterpriseId =
                u.getEnterpriseId() == null || u.getEnterpriseId() <= 0
                        ? ""
                        : String.valueOf(u.getEnterpriseId());
        return new AdminUser(
                String.valueOf(u.getId()),
                u.getUsername(),
                u.getDisplayName(),
                Role.fromString(u.getRole()),
                enterpriseId,
                canonicalEnterpriseName(u),
                Boolean.TRUE.equals(u.getDelFlag()),
                u.getCreateTime() == null ? null : ISO.format(u.getCreateTime()),
                u.getUpdateTime() == null ? null : ISO.format(u.getUpdateTime()));
    }

    /**
     * 解析企业 id 字符串；非法时返回 null。
     *
     * @param raw 客户端传入的企业 id
     * @return 解析结果
     */
    private static Long parseEnterpriseIdOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
