package com.shdata.datachain.catalog.editor;

import com.shdata.datachain.security.ApiEnvelope;
import com.shdata.datachain.security.AuthController;
import com.shdata.datachain.security.CorrelationIdSupport;
import com.shdata.datachain.security.SessionPrincipal;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 产品写 API — POST/PUT /catalog/products（鉴权由 WriteAuthorizationInterceptor）。 */
@RestController
@RequestMapping("/api/v1/catalog")
public class ProductEditorController {

  private final ProductEditorService service;

  public ProductEditorController(ProductEditorService service) {
    this.service = service;
  }

  @PostMapping("/products")
  public ResponseEntity<Map<String, Object>> create(
      @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return respond(service.create(body, actorId(request), correlationId), correlationId);
  }

  @PutMapping("/products/{productId}")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable String productId,
      @RequestBody(required = false) Map<String, Object> body,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return respond(
        service.update(productId, body, actorId(request), correlationId), correlationId);
  }

  private static String actorId(HttpServletRequest request) {
    SessionPrincipal p = AuthController.currentPrincipal(request);
    return p == null ? "-" : p.userId();
  }

  private static ResponseEntity<Map<String, Object>> respond(
      ProductEditorService.Result result, String correlationId) {
    if (result.ok()) {
      return ResponseEntity.ok(ApiEnvelope.ok(result.data(), correlationId));
    }
    return ResponseEntity.status(result.httpStatus())
        .body(ApiEnvelope.error(result.code(), result.message(), result.data(), correlationId));
  }
}
