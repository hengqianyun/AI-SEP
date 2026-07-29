package com.wsc.security;

/** V1 会话角色，对齐 contracts/rbac/matrix.yaml。 */
public enum Role {
  ADMIN,
  PROVIDER,
  USER;

  public static Role fromString(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return Role.valueOf(raw.trim().toUpperCase());
  }
}
