package com.wsc.rbac;

import com.wsc.security.Role;

/**
 * 对齐 contracts/rbac/matrix.yaml 的写权限判定。
 */
public final class RbacMatrix {

  private RbacMatrix() {}

  public static boolean canWriteCategory(Role role) {
    return role == Role.ADMIN;
  }

  public static boolean canWriteProduct(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }
}
