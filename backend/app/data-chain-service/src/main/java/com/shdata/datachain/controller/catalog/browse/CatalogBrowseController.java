package com.shdata.datachain.controller.catalog.browse;

import com.shdata.datachain.common.support.ApiEnvelope;
import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.controller.security.AuthController;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.service.catalog.browse.CatalogBrowseService;
import com.shdata.datachain.service.security.SessionAuthSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 目录浏览只读 API（mine 过滤；L2 分布见 {@link L2DistributionController}，604 独占）。
 */
@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogBrowseController {

  private final CatalogBrowseService service;

  public CatalogBrowseController(CatalogBrowseService service) {
    this.service = service;
  }

  @GetMapping("/categories")
  public ResponseEntity<Map<String, Object>> listCategories(HttpServletRequest request) {
    Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request);
    if (denied.isPresent()) {
      return denied.get();
    }
    String correlationId = CorrelationIdSupport.resolve(request);
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("items", service.listCategories());
    return ResponseEntity.ok(ApiEnvelope.ok(data, correlationId));
  }

  @GetMapping("/products")
  public ResponseEntity<Map<String, Object>> listProducts(
      @RequestParam(required = false) String l1CategoryId,
      @RequestParam(required = false) String l2CategoryId,
      @RequestParam(required = false) String l3CategoryId,
      @RequestParam(required = false) String industryCategory,
      @RequestParam(required = false) String dataSource,
      @RequestParam(required = false) String productType,
      @RequestParam(required = false) Boolean involvesPublicData,
      @RequestParam(required = false) String deliveryMethod,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Boolean mine,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      HttpServletRequest request) {
    Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request);
    if (denied.isPresent()) {
      return denied.get();
    }
    String correlationId = CorrelationIdSupport.resolve(request);
    String ownerUserId = null;
    if (Boolean.TRUE.equals(mine)) {
      SessionPrincipal principal = AuthController.currentPrincipal(request);
      ownerUserId = principal != null ? principal.userId() : null;
    }
    Map<String, Object> data =
        service.listProducts(
            l1CategoryId,
            l2CategoryId,
            l3CategoryId,
            industryCategory,
            dataSource,
            productType,
            involvesPublicData,
            deliveryMethod,
            q,
            ownerUserId,
            page,
            pageSize);
    return ResponseEntity.ok(ApiEnvelope.ok(data, correlationId));
  }

  @GetMapping("/products/{productId}")
  public ResponseEntity<Map<String, Object>> getProduct(
      @PathVariable String productId, HttpServletRequest request) {
    Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request);
    if (denied.isPresent()) {
      return denied.get();
    }
    String correlationId = CorrelationIdSupport.resolve(request);
    return service
        .getProduct(productId)
        .map(p -> ResponseEntity.ok(ApiEnvelope.ok(p, correlationId)))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiEnvelope.error("404", "产品不存在", null, correlationId)));
  }

  private static Optional<ResponseEntity<Map<String, Object>>> requireLogin(
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return SessionAuthSupport.requireLogin(request, correlationId);
  }
}
