package com.shdata.datachain.controller.catalog.admin;

import com.shdata.datachain.controller.security.AuthController;
import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.model.SessionPrincipal;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.shdata.datachain.service.catalog.admin.CategoryAdminService;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.shdata.datachain.service.catalog.admin.CategoryAdminService;
import org.springframework.web.bind.annotation.PathVariable;
import com.shdata.datachain.service.catalog.admin.CategoryAdminService;
import org.springframework.web.bind.annotation.PostMapping;
import com.shdata.datachain.service.catalog.admin.CategoryAdminService;
import org.springframework.web.bind.annotation.PutMapping;
import com.shdata.datachain.service.catalog.admin.CategoryAdminService;
import org.springframework.web.bind.annotation.RequestBody;
import com.shdata.datachain.service.catalog.admin.CategoryAdminService;
import org.springframework.web.bind.annotation.RequestMapping;
import com.shdata.datachain.service.catalog.admin.CategoryAdminService;
import org.springframework.web.bind.annotation.RestController;
import com.shdata.datachain.service.catalog.admin.CategoryAdminService;

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
    return service.create(body).toResponseEntity(correlationId);
  }

  @PutMapping("/categories/{categoryId}")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable String categoryId,
      @RequestBody(required = false) Map<String, Object> body,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return service.update(categoryId, body).toResponseEntity(correlationId);
  }

  @DeleteMapping("/categories/{categoryId}")
  public ResponseEntity<Map<String, Object>> delete(
      @PathVariable String categoryId, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    SessionPrincipal principal = AuthController.currentPrincipal(request);
    String actor = principal == null ? "-" : principal.userId();
    return service.delete(categoryId, actor, correlationId).toResponseEntity(correlationId);
  }
}
