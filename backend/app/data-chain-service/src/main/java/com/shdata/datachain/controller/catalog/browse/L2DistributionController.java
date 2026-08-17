package com.shdata.datachain.controller.catalog.browse;

import com.shdata.datachain.common.support.ApiEnvelope;
import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.service.catalog.browse.L2DistributionService;
import com.shdata.datachain.service.security.SessionAuthSupport;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * L2 产品分布 API（座序图；REQ-CAT-009 / TASK-WSC-604）。
 *
 * <p>路径独占本类；不得在 {@link CatalogBrowseController} 残留 l2-distribution。
 */
@RestController
@RequestMapping("/api/v1/catalog")
public class L2DistributionController {

  private final L2DistributionService service;

  public L2DistributionController(L2DistributionService service) {
    this.service = service;
  }

  /**
   * GET /catalog/l2-distribution — 二级目录产品计数与真实总数。
   *
   * <p>可选 {@code l1CategoryId} 将 L2 分布与 {@code totalProducts} 限定于该一级分类子树；
   * 全链口径，不按登录用户企业过滤（REQ-CAT-015 / REQ-CAT-018）。
   *
   * @param request HTTP 请求（会话鉴权）
   * @param l1CategoryId 可选一级分类 id；空则返回全平台分布
   * @return ApiEnvelope&lt;L2Distribution&gt;
   */
  @GetMapping("/l2-distribution")
  public ResponseEntity<Map<String, Object>> getL2Distribution(
      HttpServletRequest request,
      @RequestParam(value = "l1CategoryId", required = false) String l1CategoryId) {
    Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request);
    if (denied.isPresent()) {
      return denied.get();
    }
    String correlationId = CorrelationIdSupport.resolve(request);
    Map<String, Object> data = service.l2Distribution(l1CategoryId);
    return ResponseEntity.ok(ApiEnvelope.ok(data, correlationId));
  }

  private static Optional<ResponseEntity<Map<String, Object>>> requireLogin(
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return SessionAuthSupport.requireLogin(request, correlationId);
  }
}
