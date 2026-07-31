package com.shdata.datachain.catalog.admin;

import com.shdata.datachain.security.ApiEnvelope;
import com.shdata.datachain.security.AuthController;
import com.shdata.datachain.security.CorrelationIdSupport;
import com.shdata.datachain.security.SessionPrincipal;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 三级分类写 API（空间/行业/子类）— 管理员；拦截器按 matrix 返回 ERR_FORBIDDEN。 */
@RestController
@RequestMapping("/api/v1/catalog")
public class CategoryAdminController {

  private final CategoryAdminService service;

  public CategoryAdminController(CategoryAdminService service) {
    this.service = service;
  }

  @PostMapping("/categories")
  public ResponseEntity<Map<String, Object>> create(
      @RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return respond(service.create(body), correlationId);
  }

  @PutMapping("/categories/{categoryId}")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable String categoryId,
      @RequestBody(required = false) Map<String, Object> body,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return respond(service.update(categoryId, body), correlationId);
  }

  @DeleteMapping("/categories/{categoryId}")
  public ResponseEntity<Map<String, Object>> delete(
      @PathVariable String categoryId, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    SessionPrincipal principal = AuthController.currentPrincipal(request);
    String actor = principal == null ? "-" : principal.userId();
    return respond(service.delete(categoryId, actor, correlationId), correlationId);
  }

  private static ResponseEntity<Map<String, Object>> respond(
      CategoryAdminService.Result result, String correlationId) {
    if (result.ok()) {
      return ResponseEntity.ok(ApiEnvelope.ok(result.data(), correlationId));
    }
    return ResponseEntity.status(result.httpStatus())
        .body(ApiEnvelope.error(result.code(), result.message(), result.data(), correlationId));
  }
}
