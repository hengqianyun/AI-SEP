package com.shdata.datachain.security;

import java.util.LinkedHashMap;
import java.util.Map;

final class SessionViews {

  private SessionViews() {}

  static Map<String, Object> toMap(SessionPrincipal principal) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("userId", principal.userId());
    data.put("displayName", principal.displayName());
    data.put("role", principal.role().name());
    data.put(
        "enterpriseName",
        principal.enterpriseName() != null ? principal.enterpriseName() : "演示企业");
    return data;
  }
}
