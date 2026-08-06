package com.shdata.datachain.controller.catalog.editor;

import com.shdata.datachain.common.support.ApiEnvelope;
import com.shdata.datachain.controller.security.AuthController;
import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.model.SessionPrincipal;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.shdata.datachain.service.catalog.editor.ProductEditorService;
import org.springframework.web.bind.annotation.PathVariable;
import com.shdata.datachain.service.catalog.editor.ProductEditorService;
import org.springframework.web.bind.annotation.PostMapping;
import com.shdata.datachain.service.catalog.editor.ProductEditorService;
import org.springframework.web.bind.annotation.PutMapping;
import com.shdata.datachain.service.catalog.editor.ProductEditorService;
import org.springframework.web.bind.annotation.RequestBody;
import com.shdata.datachain.service.catalog.editor.ProductEditorService;
import org.springframework.web.bind.annotation.RequestMapping;
import com.shdata.datachain.service.catalog.editor.ProductEditorService;
import org.springframework.web.bind.annotation.RestController;
import com.shdata.datachain.service.catalog.editor.ProductEditorService;

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
    return service.create(body, actorId(request), correlationId).toResponseEntity(correlationId);
  }

  @PutMapping("/products/{productId}")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable String productId,
      @RequestBody(required = false) Map<String, Object> body,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return service.update(productId, body, actorId(request), correlationId).toResponseEntity(correlationId);
  }

  private static String actorId(HttpServletRequest request) {
    SessionPrincipal p = AuthController.currentPrincipal(request);
    return p == null ? "-" : p.userId();
  }
}
