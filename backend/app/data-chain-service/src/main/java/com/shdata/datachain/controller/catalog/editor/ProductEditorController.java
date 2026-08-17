package com.shdata.datachain.controller.catalog.editor;

import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.controller.security.AuthController;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.service.catalog.editor.ProductEditorService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 产品写 API — POST/PUT/DELETE /catalog/products（角色门禁由 WriteAuthorizationInterceptor；本企业/本人由 Service）。 */
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
    // 忽略 body 中的 enterpriseId；create_by 绑定会话 userId
    return service.create(body, actorId(request), correlationId).toResponseEntity(correlationId);
  }

  @PutMapping("/products/{productId}")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable String productId,
      @RequestBody(required = false) Map<String, Object> body,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return service
        .update(productId, body, AuthController.currentPrincipal(request), correlationId)
        .toResponseEntity(correlationId);
  }

  @DeleteMapping("/products/{productId}")
  public ResponseEntity<Map<String, Object>> delete(
      @PathVariable String productId, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return service
        .delete(productId, AuthController.currentPrincipal(request), correlationId)
        .toResponseEntity(correlationId);
  }

  private static String actorId(HttpServletRequest request) {
    SessionPrincipal p = AuthController.currentPrincipal(request);
    return p == null ? "-" : p.userId();
  }
}
