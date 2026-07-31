package com.shdata.datachain.security;

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

  /** §6.1 产品提交成功/失败（禁止日志明文输出信用代码等敏感字段）。 */
  public void productSubmit(
      String userId, String productCode, String result, String errorCode, String correlationId) {
    log.info(
        "event=PRODUCT_SUBMIT userId={} productCode={} result={} errorCode={} correlationId={}",
        sanitize(userId),
        sanitize(productCode),
        result,
        errorCode == null ? "-" : errorCode,
        correlationId);
  }

  /** §6.1 分类删除拒绝。 */
  public void categoryDeleteRejected(
      String userId, String categoryId, int productCount, String correlationId) {
    log.warn(
        "event=CATEGORY_DELETE_REJECTED userId={} categoryId={} productCount={} code=ERR_CATEGORY_HAS_PRODUCTS correlationId={}",
        sanitize(userId),
        sanitize(categoryId),
        productCount,
        correlationId);
  }

  private static String sanitize(String value) {
    if (value == null) {
      return "-";
    }
    return value.replaceAll("[\\r\\n\\t]", "_");
  }
}
