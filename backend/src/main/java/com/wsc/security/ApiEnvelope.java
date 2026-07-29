package com.wsc.security;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** 统一响应包装：{ code, message, data, correlationId }。 */
public final class ApiEnvelope {

  private ApiEnvelope() {}

  public static Map<String, Object> ok(Object data, String correlationId) {
    return of("0", "OK", data, correlationId);
  }

  public static Map<String, Object> error(
      String code, String message, Object data, String correlationId) {
    return of(code, message, data, correlationId);
  }

  public static Map<String, Object> of(
      String code, String message, Object data, String correlationId) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("code", code);
    body.put("message", message);
    body.put("data", data);
    body.put("correlationId", correlationId != null ? correlationId : newCorrelationId());
    return body;
  }

  public static String newCorrelationId() {
    return "corr-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }
}
