package com.wsc.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** §6.1 最低审计：登录失败、写 API 403。 */
@Component
public class AuthAuditLogger {

  private static final Logger log = LoggerFactory.getLogger("wsc.audit");

  public void loginFailed(String username, String reason, String correlationId) {
    log.warn(
        "event=AUTH_LOGIN_FAILED username={} reason={} correlationId={}",
        sanitize(username),
        reason,
        correlationId);
  }

  public void forbidden(
      String userId, Role role, String method, String path, String correlationId) {
    log.warn(
        "event=AUTH_FORBIDDEN userId={} role={} method={} resource={} code=ERR_FORBIDDEN correlationId={}",
        sanitize(userId),
        role,
        method,
        path,
        correlationId);
  }

  private static String sanitize(String value) {
    if (value == null) {
      return "-";
    }
    return value.replaceAll("[\\r\\n\\t]", "_");
  }
}
