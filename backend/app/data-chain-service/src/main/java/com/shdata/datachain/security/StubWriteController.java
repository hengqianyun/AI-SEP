package com.shdata.datachain.security;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 批量导入写路径占位（TASK-WSC-102）。
 * 目录维护真实业务已由 TASK-WSC-106 {@code catalog.maintenance} 落地；导入仍由 107 替换。
 * 写权限完全由 {@link com.shdata.datachain.rbac.WriteAuthorizationInterceptor} 判定。
 */
@RestController
@RequestMapping("/api/v1/catalog")
public class StubWriteController {

  @PostMapping("/products/import")
  public ResponseEntity<Map<String, Object>> importProducts(HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("successCount", 0);
    result.put("failureCount", 0);
    result.put("reportId", null);
    return ResponseEntity.ok(ApiEnvelope.ok(result, correlationId));
  }

  @GetMapping("/products/import/template")
  public ResponseEntity<Map<String, Object>> importTemplate(HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("format", "xlsx");
    meta.put("stub", true);
    return ResponseEntity.ok(ApiEnvelope.ok(meta, correlationId));
  }

  @GetMapping("/products/import/reports/{reportId}")
  public ResponseEntity<Map<String, Object>> importReport(
      @PathVariable String reportId, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    Map<String, Object> report = new LinkedHashMap<>();
    report.put("reportId", reportId);
    report.put("rows", List.of());
    return ResponseEntity.ok(ApiEnvelope.ok(report, correlationId));
  }
}
