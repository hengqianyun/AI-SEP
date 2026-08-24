package com.shdata.datachain.common.security;

import com.shdata.datachain.model.Role;

/**
 * 对齐 contracts/rbac/matrix.yaml（wsc-contracts@2.3.3 / SNAP-WSC-008 / PLAN-WSC-8.3 §3.1）。
 *
 * <p>V1.6：ADMIN 产品写/导入本企业 200；maintenance 按 {@code scope=full|myCatalog} 分流；
 * PROVIDER {@code scope=full} → 403，{@code scope=myCatalog} → 200。
 */
public final class RbacMatrix {

  /** 目录维护全量（仅 ADMIN）。 */
  public static final String MAINTENANCE_SCOPE_FULL = "full";

  /** 我的目录：ADMIN 本企业 / PROVIDER 本人 create_by。 */
  public static final String MAINTENANCE_SCOPE_MY_CATALOG = "myCatalog";

  private RbacMatrix() {}

  /** 三级分类维护写 — 仅管理员。 */
  public static boolean canWriteCategory(Role role) {
    return role == Role.ADMIN;
  }

  /**
   * 目录维护（无 scope 查询时的遗留默认）：ADMIN 或 PROVIDER。
   *
   * <p>PROVIDER 实际列表仍由 Service 按 create_by 过滤。带 scope 时请用 {@link
   * #canMaintainCatalog(Role, String)}。
   *
   * @param role 会话角色
   * @return 是否允许进入维护 API
   */
  public static boolean canMaintainCatalog(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /**
   * 目录维护 scope-aware：{@code full} 仅 ADMIN；{@code myCatalog} 为 ADMIN 或 PROVIDER；
   * 空/缺省 scope 走遗留默认（与无 query 的既有调用兼容）；未知 scope 拒绝。
   *
   * @param role 会话角色
   * @param scope {@code full} / {@code myCatalog} / null（缺省）
   * @return 是否允许
   */
  public static boolean canMaintainCatalog(Role role, String scope) {
    if (role == null || role == Role.USER) {
      return false;
    }
    if (scope == null || scope.isBlank()) {
      return canMaintainCatalog(role);
    }
    String normalized = scope.trim();
    if (MAINTENANCE_SCOPE_FULL.equals(normalized)) {
      return role == Role.ADMIN;
    }
    if (MAINTENANCE_SCOPE_MY_CATALOG.equals(normalized)) {
      return role == Role.ADMIN || role == Role.PROVIDER;
    }
    return false;
  }

  /** 产品新增/编辑写 — 管理员（本企业）或提供方（本人 create_by）。 */
  public static boolean canWriteProduct(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /** 批量导入写 — 管理员（本企业）或提供方。 */
  public static boolean canImportProduct(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /**
   * 我的数据产品列表（{@code mine=true}）— 管理员或提供方；USER → 403。
   *
   * @param role 会话角色
   * @return 是否允许 mine 列表
   */
  public static boolean canAccessMineApi(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /** 导入错误报告 GET — 管理员或提供方。 */
  public static boolean canGetImportReport(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /** 用户管理 — 仅管理员。 */
  public static boolean canManageUsers(Role role) {
    return role == Role.ADMIN;
  }

  // ── V1.7 订单能力方法（SNAP-WSC-009 / PLAN-WSC-9.2 §3.1 / matrix.yaml）──

  /**
   * 订单列表 API — ADMIN 全部、PROVIDER 本企业提供方、USER 本人需求方（均 200）。
   *
   * @param role 会话角色
   * @return 是否允许访问列表（scope 过滤在 Service 层）
   */
  public static boolean canListOrders(Role role) {
    return role != null;
  }

  /**
   * 创建订单 — 仅 USER（产品允许简易流程时）；PROVIDER/ADMIN → 403。
   *
   * @param role 会话角色
   * @return 是否允许创建
   */
  public static boolean canCreateOrder(Role role) {
    return role == Role.USER;
  }

  /**
   * 确认订单 — ADMIN 可代任意企业；PROVIDER 仅本企业；USER → 403。
   *
   * @param role 会话角色
   * @return 是否允许确认订单（企业范围校验在 Service 层）
   */
  public static boolean canConfirmOrder(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /**
   * 提交合约 — 仅 USER 本人需求方；ADMIN/PROVIDER → 403。
   *
   * @param role 会话角色
   * @return 是否允许提交合约
   */
  public static boolean canSubmitContract(Role role) {
    return role == Role.USER;
  }

  /**
   * 确认合约 — ADMIN 可代任意企业；PROVIDER 仅本企业；USER → 403。
   *
   * @param role 会话角色
   * @return 是否允许确认合约（企业范围校验在 Service 层）
   */
  public static boolean canConfirmContract(Role role) {
    return role == Role.ADMIN || role == Role.PROVIDER;
  }

  /**
   * 取消订单 — 三未完成态均可：USER 本人、PROVIDER 本企业、ADMIN 任意。
   *
   * @param role 会话角色
   * @return 是否允许取消（范围校验在 Service 层）
   */
  public static boolean canCancelOrder(Role role) {
    return role != null;
  }
}
