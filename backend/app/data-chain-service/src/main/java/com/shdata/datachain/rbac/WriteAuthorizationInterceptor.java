package com.shdata.datachain.rbac;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.security.ApiEnvelope;
import com.shdata.datachain.security.AuthAuditLogger;
import com.shdata.datachain.security.AuthController;
import com.shdata.datachain.security.CorrelationIdSupport;
import com.shdata.datachain.security.SessionPrincipal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 分类写 / 目录维护 / 产品写 / 批量导入 / 导入报告 GET 按 matrix.yaml 拦截；
 * 无权限返回 ERR_FORBIDDEN 或 ERR_MAINTENANCE_FORBIDDEN + correlationId。
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
    WriteResource resource = classify(request.getRequestURI());
    if (!requiresCheck(method, resource)) {
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

    boolean allowed = isAllowed(resource, principal);
    if (!allowed) {
      auditLogger.forbidden(
          principal.userId(),
          principal.role(),
          method,
          request.getRequestURI(),
          correlationId);
      String errorCode =
          resource == WriteResource.MAINTENANCE ? "ERR_MAINTENANCE_FORBIDDEN" : "ERR_FORBIDDEN";
      String message =
          resource == WriteResource.MAINTENANCE
              ? "当前角色无权执行目录维护操作"
              : "当前角色无权执行该写操作";
      writeJson(
          response,
          HttpServletResponse.SC_FORBIDDEN,
          ApiEnvelope.error(errorCode, message, null, correlationId));
      return false;
    }
    return true;
  }

  static boolean requiresCheck(String method, WriteResource resource) {
    if (resource == WriteResource.NONE) {
      return false;
    }
    if ("OPTIONS".equalsIgnoreCase(method)) {
      return false;
    }
    return switch (resource) {
        // 分类 / 产品：仅写方法
      case CATEGORY, PRODUCT -> !isSafeMethod(method);
        // 目录维护：含列表 GET（契约 403）
      case MAINTENANCE -> true;
        // 导入 POST + 模板 GET
      case IMPORT -> true;
        // 错误报告 GET
      case IMPORT_REPORT -> true;
      case NONE -> false;
    };
  }

  private static boolean isSafeMethod(String method) {
    return "GET".equalsIgnoreCase(method)
        || "HEAD".equalsIgnoreCase(method)
        || "OPTIONS".equalsIgnoreCase(method);
  }

  private static boolean isAllowed(WriteResource resource, SessionPrincipal principal) {
    return switch (resource) {
      case CATEGORY -> RbacMatrix.canWriteCategory(principal.role());
      case PRODUCT -> RbacMatrix.canWriteProduct(principal.role());
      case MAINTENANCE -> RbacMatrix.canMaintainCatalog(principal.role());
      case IMPORT -> RbacMatrix.canImportProduct(principal.role());
      case IMPORT_REPORT -> RbacMatrix.canGetImportReport(principal.role());
      case NONE -> true;
    };
  }

  static WriteResource classify(String uri) {
    if (uri == null) {
      return WriteResource.NONE;
    }
    String path = uri;
    int idx = path.indexOf("/api/v1/");
    if (idx >= 0) {
      path = path.substring(idx);
    }
    if (path.startsWith("/api/v1/catalog/categories")) {
      return WriteResource.CATEGORY;
    }
    if (path.startsWith("/api/v1/catalog/maintenance")) {
      return WriteResource.MAINTENANCE;
    }
    // 须先于 /catalog/products 通用匹配
    if (path.startsWith("/api/v1/catalog/products/import/reports")) {
      return WriteResource.IMPORT_REPORT;
    }
    if (path.startsWith("/api/v1/catalog/products/import")) {
      return WriteResource.IMPORT;
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
    MAINTENANCE,
    IMPORT,
    IMPORT_REPORT,
    NONE
  }
}
