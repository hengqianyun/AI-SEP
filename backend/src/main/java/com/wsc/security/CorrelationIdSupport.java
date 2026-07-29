package com.wsc.security;

import jakarta.servlet.http.HttpServletRequest;

public final class CorrelationIdSupport {

  public static final String HEADER = "X-Correlation-Id";
  public static final String ATTR = "wsc.correlationId";

  private CorrelationIdSupport() {}

  public static String resolve(HttpServletRequest request) {
    Object existing = request.getAttribute(ATTR);
    if (existing instanceof String s && !s.isBlank()) {
      return s;
    }
    String fromHeader = request.getHeader(HEADER);
    String id =
        (fromHeader != null && !fromHeader.isBlank())
            ? fromHeader.trim()
            : ApiEnvelope.newCorrelationId();
    request.setAttribute(ATTR, id);
    return id;
  }
}
