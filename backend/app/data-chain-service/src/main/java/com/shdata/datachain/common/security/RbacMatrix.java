package com.shdata.datachain.common.security;

import com.shdata.datachain.model.Role;

/**
 * 对齐 contracts/rbac/matrix.yaml（wsc-contracts@2.2.0 / SNAP-WSC-005 / TASK-WSC-607）。
 */
public final class RbacMatrix {

  private RbacMatrix() {}

  /** 三级分类维护写 — 仅管理员。 */
  public static boolean canWriteCategory(Role role) {
    return role == Role.ADMIN;
  }

  /** 目录维护（关联）— 管理员或提供方（提供方仅本人产品，由 Service 再过滤）。 */
  public static boolean canMaintainCatalog(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /** 产品新增/编辑写 — 仅提供方（V1.4）。 */
  public static boolean canWriteProduct(Role role) {
    return role == Role.PROVIDER;
  }

  /** 批量导入写 — 仅提供方（V1.4）。 */
  public static boolean canImportProduct(Role role) {
    return role == Role.PROVIDER;
  }

  /** 导入错误报告 GET — 管理员或提供方。 */
  public static boolean canGetImportReport(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /** 用户管理 — 仅管理员。 */
  public static boolean canManageUsers(Role role) {
    return role == Role.ADMIN;
  }
}
