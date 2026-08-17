package com.shdata.datachain.controller.catalog.maintenance;

import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.controller.security.AuthController;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.service.catalog.maintenance.CatalogMaintenanceService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 目录维护 API（REQ-CAT-007 / REQ-CAT-017 / TASK-WSC-907）。
 *
 * <p>角色门禁由 {@code WriteAuthorizationInterceptor}（含 GET）按 query {@code scope} 执行；
 * 本企业 / 本人结果集由 Service 按会话推导。忽略客户端 {@code enterpriseId}。
 */
@RestController
@RequestMapping("/api/v1/catalog/maintenance")
public class CatalogMaintenanceController {

  private final CatalogMaintenanceService service;

  /**
   * @param service 目录维护业务
   */
  public CatalogMaintenanceController(CatalogMaintenanceService service) {
    this.service = service;
  }

  /**
   * 分页列表：{@code scope=full} 为 ADMIN 全平台；{@code scope=myCatalog} 为 ADMIN 本企业 /
   * PROVIDER 本人 create_by。缺省 scope 保持遗留：ADMIN 全量、PROVIDER 本人。
   *
   * @param status       维护状态 ALL / MAINTAINED / PENDING
   * @param l1CategoryId 一级分类（可选）
   * @param l2CategoryId 二级分类（可选）
   * @param l3CategoryId 三级分类（可选）
   * @param page         页码
   * @param pageSize     每页条数
   * @param scope        {@code full} / {@code myCatalog}；授权仅信会话
   * @param request      HTTP 请求（取会话与 correlationId）
   * @return 分页条目
   */
  @GetMapping("/entries")
  public ResponseEntity<Map<String, Object>> list(
      @RequestParam(required = false, defaultValue = "ALL") String status,
      @RequestParam(required = false) String l1CategoryId,
      @RequestParam(required = false) String l2CategoryId,
      @RequestParam(required = false) String l3CategoryId,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "20") int pageSize,
      @RequestParam(required = false) String scope,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return service
        .list(
            status,
            l1CategoryId,
            l2CategoryId,
            l3CategoryId,
            page,
            pageSize,
            currentActor(request),
            scope)
        .toResponseEntity(correlationId);
  }

  /**
   * 单条挂载三级：PROVIDER 越权 → 403；ADMIN {@code myCatalog} 跨企业 → 403。
   *
   * @param productId 产品 id 或编码
   * @param body      须含 {@code l3CategoryId}
   * @param scope     与列表相同的维护 scope
   * @param request   HTTP 请求
   * @return 挂载后的条目
   */
  @PutMapping("/entries/{productId}")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable String productId,
      @RequestBody(required = false) Map<String, Object> body,
      @RequestParam(required = false) String scope,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return service
        .associate(productId, body, currentActor(request), scope)
        .toResponseEntity(correlationId);
  }

  /**
   * 批量挂载：逐条归属校验，失败进入 failures。
   *
   * @param body    须含 {@code productIds} 与 {@code l3CategoryId}
   * @param scope   与列表相同的维护 scope
   * @param request HTTP 请求
   * @return 成功/失败计数
   */
  @PostMapping("/entries/batch")
  public ResponseEntity<Map<String, Object>> batch(
      @RequestBody(required = false) Map<String, Object> body,
      @RequestParam(required = false) String scope,
      HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return service
        .batchAssociate(body, currentActor(request), scope)
        .toResponseEntity(correlationId);
  }

  /**
   * 从会话取主体；未登录时为 null（拦截器通常已拦）。
   *
   * @param request HTTP 请求
   * @return 会话主体
   */
  private static SessionPrincipal currentActor(HttpServletRequest request) {
    return AuthController.currentPrincipal(request);
  }
}
