package com.wsc.security;

import java.io.Serializable;

/** 绑定在 HttpSession 上的当前主体。 */
public record SessionPrincipal(
    String userId, String displayName, Role role, String enterpriseName)
    implements Serializable {

  private static final long serialVersionUID = 1L;
}
