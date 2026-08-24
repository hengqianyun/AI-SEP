package com.shdata.datachain.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.common.support.ApiEnvelope;
import com.shdata.datachain.common.security.AuthAuditLogger;
import com.shdata.datachain.controller.security.AuthController;
import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.model.SessionPrincipal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 分类写 / 目录维护 / 产品写 / 批量导入 / 导入报告 GET 按 matrix.yaml 拦截。
 *
 * <p>V1.6：维护 API 按 query {@code scope=full|myCatalog} 分流（PROVIDER {@code full}→403）；
 * 产品写/导入允许 ADMIN（本企业校验在 Service）。授权仅信 {@link SessionPrincipal}，
 * 忽略客户端 {@code enterpriseId} query/body。
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

    boolean allowed = isAllowed(resource, principal, request);
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
        // 订单创建：仅 POST 拦截（列表/详情 GET 不经拦截器）
      case ORDER_CREATE -> !isSafeMethod(method);
        // 订单确认/提交合约/确认合约/取消：仅 POST 拦截
      case ORDER_CONFIRM, ORDER_CONTRACT, ORDER_CONFIRM_CONTRACT, ORDER_CANCEL -> !isSafeMethod(method);
      case NONE -> false;
    };
  }

  private static boolean isSafeMethod(String method) {
    return "GET".equalsIgnoreCase(method)
        || "HEAD".equalsIgnoreCase(method)
        || "OPTIONS".equalsIgnoreCase(method);
  }

  /**
   * 按资源与会话判定是否放行。维护资源读取 query {@code scope}；不读取客户端 enterpriseId。
   *
   * @param resource 写资源分类
   * @param principal 会话主体
   * @param request 用于读取 maintenance {@code scope} query
   * @return 是否允许
   */
  static boolean isAllowed(
      WriteResource resource, SessionPrincipal principal, HttpServletRequest request) {
    return switch (resource) {
      case CATEGORY -> RbacMatrix.canWriteCategory(principal.role());
      case PRODUCT -> RbacMatrix.canWriteProduct(principal.role());
      case MAINTENANCE ->
          RbacMatrix.canMaintainCatalog(principal.role(), maintenanceScope(request));
      case IMPORT -> RbacMatrix.canImportProduct(principal.role());
      case IMPORT_REPORT -> RbacMatrix.canGetImportReport(principal.role());
      case ORDER_CREATE -> RbacMatrix.canCreateOrder(principal.role());
      case ORDER_CONFIRM -> RbacMatrix.canConfirmOrder(principal.role());
      case ORDER_CONTRACT -> RbacMatrix.canSubmitContract(principal.role());
      case ORDER_CONFIRM_CONTRACT -> RbacMatrix.canConfirmContract(principal.role());
      case ORDER_CANCEL -> RbacMatrix.canCancelOrder(principal.role());
      case NONE -> true;
    };
  }

  /**
   * 读取维护 scope query；缺省返回 null（矩阵走遗留默认）。忽略 {@code enterpriseId}。
   *
   * @param request 当前请求
   * @return {@code full} / {@code myCatalog} / null
   */
  static String maintenanceScope(HttpServletRequest request) {
    if (request == null) {
      return null;
    }
    String scope = request.getParameter("scope");
    if (scope == null || scope.isBlank()) {
      return null;
    }
    return scope.trim();
  }

  /**
   * 按 URI 分类写资源（导入路径须先于通用 /catalog/products）。
   *
   * @param uri 请求 URI
   * @return 资源类型
   */
  public static WriteResource classify(String uri) {
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
    // 订单写路径细分（916 扩展：confirm/contract/confirm-contract/cancel）
    if (path.startsWith("/api/v1/orders") && !path.contains("/notices/")) {
      // 按路径后缀细分资源类型，确保不同写操作走不同权限检查
      if (path.endsWith("/confirm") && !path.endsWith("/contract/confirm")) {
        return WriteResource.ORDER_CONFIRM;
      }
      if (path.endsWith("/contract/confirm")) {
        return WriteResource.ORDER_CONFIRM_CONTRACT;
      }
      if (path.endsWith("/contract")) {
        return WriteResource.ORDER_CONTRACT;
      }
      if (path.endsWith("/cancel")) {
        return WriteResource.ORDER_CANCEL;
      }
      return WriteResource.ORDER_CREATE;
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

  /** 写资源分类（供拦截器与矩阵测试使用）。 */
  public enum WriteResource {
    CATEGORY,
    PRODUCT,
    MAINTENANCE,
    IMPORT,
    IMPORT_REPORT,
    /** 订单创建（POST /orders）。 */
    ORDER_CREATE,
    /** 确认订单（POST /orders/{id}/confirm）。 */
    ORDER_CONFIRM,
    /** 提交合约附件与交易信息（POST /orders/{id}/contract）。 */
    ORDER_CONTRACT,
    /** 统一确认合约（POST /orders/{id}/contract/confirm）。 */
    ORDER_CONFIRM_CONTRACT,
    /** 取消订单（POST /orders/{id}/cancel）。 */
    ORDER_CANCEL,
    NONE
  }
}
