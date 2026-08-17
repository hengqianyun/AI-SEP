package com.shdata.datachain.model;

import java.io.Serializable;

/**
 * 绑定在 HttpSession 上的当前主体。
 *
 * <p>{@code enterpriseId} + {@code enterpriseName} 为 mine/myCatalog/写 scope 单一真源（REQ-USER-002）。
 */
public record SessionPrincipal(
    String userId, String displayName, Role role, String enterpriseId, String enterpriseName)
    implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 规范化空企业字段，避免序列化 null（空串表示异常占位，服务层应写入真实 id）。
   *
   * @param userId 用户 id
   * @param displayName 显示名
   * @param role 角色
   * @param enterpriseId 企业 id 字符串
   * @param enterpriseName 企业名称
   */
  public SessionPrincipal {
    if (enterpriseId == null) {
      enterpriseId = "";
    }
    if (enterpriseName == null) {
      enterpriseName = "";
    }
  }
}
