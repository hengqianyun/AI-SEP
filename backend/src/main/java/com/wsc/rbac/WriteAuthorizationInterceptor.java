package com.wsc.rbac;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsc.security.ApiEnvelope;
import com.wsc.security.AuthAuditLogger;
import com.wsc.security.AuthController;
import com.wsc.security.CorrelationIdSupport;
import com.wsc.security.SessionPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 分类写 / 产品写路径按 matrix.yaml 拦截；无权限返回 ERR_FORBIDDEN + correlationId。
 */
@Component
public class WriteAuthorizationInterceptor implements HandlerInterceptor {

  private final AuthAuditLogger auditLogger;
  private final ObjectMapper objectMapper;

  public WriteAuthorizationInterceptor(AuthAuditLogger auditLogger, ObjectMapper objectMapper) {
    this.auditLogger = auditLogger;
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String method = request.getMethod();
    if ("GET".equalsIgnoreCase(method)
        || "HEAD".equalsIgnoreCase(method)
        || "OPTIONS".equalsIgnoreCase(method)) {
      return true;
    }

    WriteResource resource = classify(request.getRequestURI());
    if (resource == WriteResource.NONE) {
      return true;
    }

    String correlationId = CorrelationIdSupport.resolve(request);
    SessionPrincipal principal = AuthController.currentPrincipal(request);
    if (principal == null) {
      writeJson(
          response,
          HttpServletResponse.SC_UNAUTHORIZED,
          ApiEnvelope.error("401", "未登录或会话已失效", null, correlationId));
      return false;
    }

    boolean allowed =
        switch (resource) {
          case CATEGORY -> RbacMatrix.canWriteCategory(principal.role());
          case PRODUCT -> RbacMatrix.canWriteProduct(principal.role());
          case NONE -> true;
        };

    if (!allowed) {
      auditLogger.forbidden(
          principal.userId(),
          principal.role(),
          method,
          request.getRequestURI(),
          correlationId);
      writeJson(
          response,
          HttpServletResponse.SC_FORBIDDEN,
          ApiEnvelope.error(
              "ERR_FORBIDDEN", "当前角色无权执行该写操作", null, correlationId));
      return false;
    }
    return true;
  }

  static WriteResource classify(String uri) {
    if (uri == null) {
      return WriteResource.NONE;
    }
    // 去掉 context-path 后匹配 /api/v1/...
    String path = uri;
    int idx = path.indexOf("/api/v1/");
    if (idx >= 0) {
      path = path.substring(idx);
    }
    if (path.startsWith("/api/v1/catalog/categories")) {
      return WriteResource.CATEGORY;
    }
    if (path.startsWith("/api/v1/catalog/products")) {
      return WriteResource.PRODUCT;
    }
    return WriteResource.NONE;
  }

  private void writeJson(HttpServletResponse response, int status, Map<String, Object> body)
      throws Exception {
    response.setStatus(status);
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), body);
  }

  enum WriteResource {
    CATEGORY,
    PRODUCT,
    NONE
  }
}
