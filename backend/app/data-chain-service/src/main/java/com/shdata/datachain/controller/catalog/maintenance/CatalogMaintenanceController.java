package com.shdata.datachain.controller.catalog.maintenance;

import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.controller.security.AuthController;
import com.shdata.datachain.model.Role;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.service.catalog.maintenance.CatalogMaintenanceService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 目录维护 API（REQ-CAT-007 / TASK-WSC-607）。
 * <p>角色门禁由 {@code WriteAuthorizationInterceptor}（含 GET）；PROVIDER 本人隔离由 Service 执行。</p>
 */
@RestController
@RequestMapping("/api/v1/catalog/maintenance")
public class CatalogMaintenanceController {

  private final CatalogMaintenanceService service;

  public CatalogMaintenanceController(CatalogMaintenanceService service) {
    this.service = service;
  }

  /**
   * 分页列表：ADMIN 全量；PROVIDER 仅 create_by=本人。
   */
  @GetMapping("/entries")
  public ResponseEntity<Map<String, Object>> list(
      @RequestParam(required = false, defaultValue = "ALL") String status,
      @RequestParam(required = false) String l1CategoryId,
      @RequestParam(required = false) String l2CategoryId,
      @RequestParam(required = false) String l3CategoryId,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "20") int pageSize,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return service
        .list(
            status,
            l1CategoryId,
            l2CategoryId,
            l3CategoryId,
            page,
            pageSize,
            actorRole(request),
            actorId(request))
        .toResponseEntity(correlationId);
  }

  /**
   * 单条挂载三级：PROVIDER 越权 → 403。
   */
  @PutMapping("/entries/{productId}")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable String productId,
      @RequestBody(required = false) Map<String, Object> body,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return service
        .associate(productId, body, actorRole(request), actorId(request))
        .toResponseEntity(correlationId);
  }

  /**
   * 批量挂载：PROVIDER 逐条归属校验，失败进入 failures。
   */
  @PostMapping("/entries/batch")
  public ResponseEntity<Map<String, Object>> batch(
      @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return service
        .batchAssociate(body, actorRole(request), actorId(request))
        .toResponseEntity(correlationId);
  }

  private static String actorId(HttpServletRequest request) {
    SessionPrincipal p = AuthController.currentPrincipal(request);
    return p == null ? "-" : p.userId();
  }

  private static Role actorRole(HttpServletRequest request) {
    SessionPrincipal p = AuthController.currentPrincipal(request);
    return p == null ? null : p.role();
  }
}
