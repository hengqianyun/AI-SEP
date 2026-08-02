package com.shdata.datachain.catalog.productimport;

import com.shdata.datachain.security.ApiEnvelope;
import com.shdata.datachain.security.AuthController;
import com.shdata.datachain.security.CorrelationIdSupport;
import com.shdata.datachain.security.SessionPrincipal;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 批量导入 API — /catalog/products/import*（鉴权由 WriteAuthorizationInterceptor）。 */
@RestController
@RequestMapping("/api/v1/catalog/products/import")
public class ProductImportController {

  private final ProductImportService service;

  public ProductImportController(ProductImportService service) {
    this.service = service;
  }

  @GetMapping("/template")
  public ResponseEntity<byte[]> template(
      @RequestParam(defaultValue = "xlsx") String format, HttpServletRequest request)
      throws Exception {
    byte[] bytes = service.templateBytes(format);
    boolean csv = "csv".equalsIgnoreCase(format);
    MediaType media =
        csv
            ? new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8)
            : MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    String filename = csv ? "product-import-template.csv" : "product-import-template.xlsx";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(media)
        .body(bytes);
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> importProducts(
      @RequestParam(value = "file", required = false) MultipartFile file,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    SessionPrincipal principal = AuthController.currentPrincipal(request);
    String actor = principal == null ? "-" : principal.userId();
    // file 缺失：请求级格式错误（非 stub 空成功）。SessionAuth 矩阵探测请带 multipart。
    return respond(service.importFile(file, actor, correlationId), correlationId);
  }

  @GetMapping("/reports/{reportId}")
  public ResponseEntity<Map<String, Object>> getReport(
      @PathVariable String reportId, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    SessionPrincipal principal = AuthController.currentPrincipal(request);
    String actor = principal == null ? "-" : principal.userId();
    var role = principal == null ? null : principal.role();
    return respond(service.getReport(reportId, actor, role), correlationId);
  }

  private static ResponseEntity<Map<String, Object>> respond(
      ProductImportService.Result result, String correlationId) {
    if (result.ok()) {
      return ResponseEntity.ok(ApiEnvelope.ok(result.data(), correlationId));
    }
    return ResponseEntity.status(result.httpStatus())
        .body(ApiEnvelope.error(result.code(), result.message(), result.data(), correlationId));
  }
}
