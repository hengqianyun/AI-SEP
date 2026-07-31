package com.shdata.datachain.security;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 演示账号三角色（内存）。用户名：admin / provider / user；密码均为 demo。
 * 登录请求可带 role 覆盖默认角色（演示切换）。
 */
@Component
public class DemoAccountService {

  public static final String DEMO_PASSWORD = "demo";

  private final Map<String, DemoAccount> accounts = new LinkedHashMap<>();

  public DemoAccountService() {
    accounts.put(
        "admin",
        new DemoAccount("u-admin", "admin", "演示管理员", Role.ADMIN, "演示企业"));
    accounts.put(
        "provider",
        new DemoAccount("u-provider", "provider", "演示提供方", Role.PROVIDER, "演示企业"));
    accounts.put(
        "user",
        new DemoAccount("u-user", "user", "演示普通用户", Role.USER, "演示企业"));
  }

  public Optional<SessionPrincipal> authenticate(String username, String password, Role roleOverride) {
    if (username == null || password == null) {
      return Optional.empty();
    }
    DemoAccount account = accounts.get(username.trim().toLowerCase());
    if (account == null || !DEMO_PASSWORD.equals(password)) {
      return Optional.empty();
    }
    Role role = roleOverride != null ? roleOverride : account.defaultRole();
    return Optional.of(
        new SessionPrincipal(
            account.userId(), account.displayName(), role, account.enterpriseName()));
  }

  public Optional<SessionPrincipal> switchRole(SessionPrincipal current, Role newRole) {
    if (current == null || newRole == null) {
      return Optional.empty();
    }
    // 演示：保留身份，仅更换会话角色
    return Optional.of(
        new SessionPrincipal(
            current.userId(), current.displayName(), newRole, current.enterpriseName()));
  }

  public Collection<DemoAccount> list() {
    return accounts.values();
  }

  public record DemoAccount(
      String userId, String username, String displayName, Role defaultRole, String enterpriseName) {}
}
