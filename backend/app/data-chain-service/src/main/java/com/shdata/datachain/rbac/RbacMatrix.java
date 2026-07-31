package com.shdata.datachain.rbac;

import com.shdata.datachain.security.Role;

/**
 * 对齐 contracts/rbac/matrix.yaml（wsc-contracts@2.0.0 / PLAN-WSC-2.2 §4）的写权限判定。
 */
public final class RbacMatrix {

  private RbacMatrix() {}

  /** 三级分类维护写 — 仅管理员。 */
  public static boolean canWriteCategory(Role role) {
    return role == Role.ADMIN;
  }

  /** 目录维护（关联）— 仅管理员。 */
  public static boolean canMaintainCatalog(Role role) {
    return role == Role.ADMIN;
  }

  /** 产品新增/编辑写 — 管理员与提供方。 */
  public static boolean canWriteProduct(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /** 批量导入写 — 管理员与提供方。 */
  public static boolean canImportProduct(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /** 导入错误报告 GET — 管理员或具备导入权限的提供方。 */
  public static boolean canGetImportReport(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }
}
