package com.shdata.datachain.catalog.maintenance;

import com.shdata.datachain.security.ApiEnvelope;
import com.shdata.datachain.security.CorrelationIdSupport;
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
 * 目录维护 API（REQ-CAT-007）。权限由 {@code WriteAuthorizationInterceptor} 拦截（含 GET）。
 */
@RestController
@RequestMapping("/api/v1/catalog/maintenance")
public class CatalogMaintenanceController {

  private final CatalogMaintenanceService service;

  public CatalogMaintenanceController(CatalogMaintenanceService service) {
    this.service = service;
  }

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
    return respond(
        service.list(status, l1CategoryId, l2CategoryId, l3CategoryId, page, pageSize),
        correlationId);
  }

  @PutMapping("/entries/{productId}")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable String productId,
      @RequestBody(required = false) Map<String, Object> body,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return respond(service.associate(productId, body), correlationId);
  }

  @PostMapping("/entries/batch")
  public ResponseEntity<Map<String, Object>> batch(
      @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return respond(service.batchAssociate(body), correlationId);
  }

  private static ResponseEntity<Map<String, Object>> respond(
      CatalogMaintenanceService.Result result, String correlationId) {
    if (result.ok()) {
      return ResponseEntity.ok(ApiEnvelope.ok(result.data(), correlationId));
    }
    return ResponseEntity.status(result.httpStatus())
        .body(ApiEnvelope.error(result.code(), result.message(), result.data(), correlationId));
  }
}
