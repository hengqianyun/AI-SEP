package com.wsc.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 占位写端点，供 TASK-WSC-002 矩阵测试。鉴权由 WriteAuthorizationInterceptor 拦截。
 * 故意放在 security 包，避免写入 catalog feature 包。
 */
@RestController
@RequestMapping("/api/v1/catalog")
public class StubWriteController {

  @PostMapping("/categories")
  public ResponseEntity<Map<String, Object>> createCategory(HttpServletRequest request) {
    return okStub(request, "category", "stub-category");
  }

  @PutMapping("/categories/{categoryId}")
  public ResponseEntity<Map<String, Object>> updateCategory(
      @PathVariable String categoryId, HttpServletRequest request) {
    return okStub(request, "category", categoryId);
  }

  @PostMapping("/products")
  public ResponseEntity<Map<String, Object>> createProduct(HttpServletRequest request) {
    return okStub(request, "product", "stub-product");
  }

  @PutMapping("/products/{productId}")
  public ResponseEntity<Map<String, Object>> updateProduct(
      @PathVariable String productId, HttpServletRequest request) {
    return okStub(request, "product", productId);
  }

  private static ResponseEntity<Map<String, Object>> okStub(
      HttpServletRequest request, String kind, String id) {
    String correlationId = CorrelationIdSupport.resolve(request);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("id", id);
    data.put("stub", true);
    data.put("kind", kind);
    return ResponseEntity.ok(ApiEnvelope.ok(data, correlationId));
  }
}
