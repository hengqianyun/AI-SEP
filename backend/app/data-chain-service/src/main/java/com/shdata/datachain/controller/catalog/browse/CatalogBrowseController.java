package com.shdata.datachain.controller.catalog.browse;

import com.shdata.datachain.common.security.RbacMatrix;
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
 * 目录浏览只读 API（mine=本企业；全链无企业 filter；L2 分布见 {@link L2DistributionController}）。
 *
 * <p>授权仅信 {@link SessionPrincipal}；客户端 {@code enterpriseId}/{@code enterpriseName} query
 * 不作为过滤或授权依据。
 */
@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogBrowseController {

  private final CatalogBrowseService service;

  public CatalogBrowseController(CatalogBrowseService service) {
    this.service = service;
  }

  /**
   * 列出三级分类树。
   *
   * @param request 当前请求（须登录）
   * @return 分类列表
   */
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

  /**
   * 产品列表。全链默认全平台；{@code mine=true} 仅本企业（ADMIN/PROVIDER）；USER mine → 403。
   *
   * <p>不绑定 {@code enterpriseId}/{@code enterpriseName}：即使客户端传入也忽略。
   *
   * @param l1CategoryId 一级分类
   * @param l2CategoryId 二级分类
   * @param l3CategoryId 三级分类
   * @param industryCategory 行业门类（浏览场景弃用，schema 保留）
   * @param dataSource 数据来源
   * @param productType 产品类型
   * @param involvesPublicData 是否涉公
   * @param deliveryMethod 交付方式
   * @param q 名称/编码关键字
   * @param supplierName 供应方名称模糊匹配（REQ-CAT-012）
   * @param mine true 时按会话企业过滤
   * @param page 页码
   * @param pageSize 页大小
   * @param request 当前请求
   * @return 分页列表（含 l3Counts 全集计数）
   */
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
      @RequestParam(required = false) String supplierName,
      @RequestParam(required = false) Boolean mine,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize,
      HttpServletRequest request) {
    Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request);
    if (denied.isPresent()) {
      return denied.get();
    }
    String correlationId = CorrelationIdSupport.resolve(request);
    String mineEnterpriseId = null;
    if (Boolean.TRUE.equals(mine)) {
      SessionPrincipal principal = AuthController.currentPrincipal(request);
      if (principal == null || !RbacMatrix.canAccessMineApi(principal.role())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiEnvelope.error("ERR_FORBIDDEN", "当前角色无权查看我的数据产品", null, correlationId));
      }
      // 仅信会话 enterpriseId；忽略 request 中的企业参数
      mineEnterpriseId = principal.enterpriseId();
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
            supplierName,
            mineEnterpriseId,
            page,
            pageSize);
    return ResponseEntity.ok(ApiEnvelope.ok(data, correlationId));
  }

  /**
   * 产品详情。
   *
   * @param productId 产品 id
   * @param request 当前请求
   * @return 产品或 404
   */
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
