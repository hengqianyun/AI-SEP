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
   * @param request HTTP 请求（会话鉴权）
   * @return ApiEnvelope&lt;L2Distribution&gt;
   */
  @GetMapping("/l2-distribution")
  public ResponseEntity<Map<String, Object>> getL2Distribution(HttpServletRequest request) {
    Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request);
    if (denied.isPresent()) {
      return denied.get();
    }
    String correlationId = CorrelationIdSupport.resolve(request);
    Map<String, Object> data = service.l2Distribution();
    return ResponseEntity.ok(ApiEnvelope.ok(data, correlationId));
  }

  private static Optional<ResponseEntity<Map<String, Object>>> requireLogin(
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return SessionAuthSupport.requireLogin(request, correlationId);
  }
}
