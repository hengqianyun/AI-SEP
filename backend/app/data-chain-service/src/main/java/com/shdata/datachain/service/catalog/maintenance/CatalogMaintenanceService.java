package com.shdata.datachain.service.catalog.maintenance;

import com.shdata.datachain.common.exception.BusinessException;
import com.shdata.datachain.common.response.ServiceResult;
import com.shdata.datachain.common.security.EnterpriseProductScope;
import com.shdata.datachain.common.security.RbacMatrix;
import com.shdata.datachain.model.CatalogCategory;
import com.shdata.datachain.model.CatalogProduct;
import com.shdata.datachain.model.Role;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import com.shdata.datachain.service.catalog.editor.ProductEditorService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

/**
 * 目录维护：列表筛选/分页、单条与批量挂载三级（REQ-CAT-007 / REQ-CAT-017 / TASK-WSC-907）。
 *
 * <p>Tips：
 * <ul>
 *   <li>{@link #list} — {@code scope=full} ADMIN 全平台；{@code myCatalog} ADMIN 本企业 / PROVIDER 本人 create_by</li>
 *   <li>本企业判定复用 {@link EnterpriseProductScope}（create_by → sys_user.enterprise_id）</li>
 *   <li>维护状态：有有效 {@code l3CategoryId} → MAINTAINED，否则 PENDING</li>
 * </ul>
 */
@Service
public class CatalogMaintenanceService {

  private final CatalogBrowseSeedStore catalog;
  private final ProductEditorService productEditorService;
  private final EnterpriseProductScope enterpriseScope;

  /**
   * @param catalog 目录存储
   * @param productEditorService 产品编辑服务（挂载后异步上链）
   * @param enterpriseScope 本企业产品判定（§3.2）
   */
  public CatalogMaintenanceService(
      CatalogBrowseSeedStore catalog,
      ProductEditorService productEditorService,
      EnterpriseProductScope enterpriseScope) {
    this.catalog = catalog;
    this.productEditorService = productEditorService;
    this.enterpriseScope = enterpriseScope;
  }

  /** 演示用待关联条目（不改 browse seed 源码；仅 upsert 缺三级产品）。 */
  @PostConstruct
  void ensurePendingSamples() {
    if (catalog.products().stream().anyMatch(p -> p.l3CategoryId() == null || p.l3CategoryId().isBlank())) {
      return;
    }
    catalog.upsertProduct(pendingProduct("prod-pending-001", "PEND-0001", "政务授权核验数据目录"));
    catalog.upsertProduct(pendingProduct("prod-pending-002", "PEND-0002", "供应链溯源数据目录"));
    catalog.upsertProduct(pendingProduct("prod-pending-003", "PEND-0003", "跨境物流追踪数据目录"));
  }

  /**
   * 分页筛选产品维护条目。
   *
   * @param status       维护状态：ALL / MAINTAINED / PENDING
   * @param l1CategoryId 一级分类筛选（可选）
   * @param l2CategoryId 二级分类筛选（可选）
   * @param l3CategoryId 三级分类筛选（可选）
   * @param page         页码（1-based）
   * @param pageSize     每页条数（≤100）
   * @param actor        会话主体（scope 仅信此对象的企业/角色，忽略客户端 enterpriseId）
   * @param scope        {@code full} / {@code myCatalog}；空则遗留默认
   * @return 分页结果
   */
  public ServiceResult list(
      String status,
      String l1CategoryId,
      String l2CategoryId,
      String l3CategoryId,
      int page,
      int pageSize,
      SessionPrincipal actor,
      String scope) {
    String statusNorm = normalizeStatus(status);
    int safePage = Math.max(page, 1);
    int safeSize = Math.min(Math.max(pageSize, 1), 100);

    List<CatalogProduct> source = sourceProducts(actor, scope);

    List<CatalogProduct> filtered =
        source.stream()
            .filter(p -> matchesStatus(statusNorm, p))
            .filter(p -> matchesL1(l1CategoryId, p))
            .filter(p -> matchesL2(l2CategoryId, p))
            .filter(p -> matches(l3CategoryId, p.l3CategoryId()))
            .collect(Collectors.toList());

    long total = filtered.size();
    int from = Math.min((safePage - 1) * safeSize, filtered.size());
    int to = Math.min(from + safeSize, filtered.size());
    List<Map<String, Object>> items =
        filtered.subList(from, to).stream().map(this::toEntry).collect(Collectors.toList());

    Map<String, Object> pageBody = new LinkedHashMap<>();
    pageBody.put("items", items);
    pageBody.put("page", safePage);
    pageBody.put("pageSize", safeSize);
    pageBody.put("total", total);
    return ServiceResult.ok(pageBody);
  }

  /**
   * 将单条产品挂载到指定三级分类。
   *
   * @param productId 产品 ID
   * @param body      请求体，须含 {@code l3CategoryId}
   * @param actor     会话主体
   * @param scope     {@code full} / {@code myCatalog}
   * @return 挂载后的产品条目
   */
  public ServiceResult associate(
      String productId, Map<String, Object> body, SessionPrincipal actor, String scope) {
    try {
      requireWritable(productId, actor, scope);
      String l3Id = requireL3Id(body);
      String actorUserId = actor == null ? "-" : actor.userId();
      CatalogProduct updated = applyAssociate(productId, l3Id, actorUserId);
      return ServiceResult.ok(toEntry(updated));
    } catch (BusinessException ex) {
      return ServiceResult.fail(ex.httpStatus(), ex.code(), ex.getMessage(), ex.data());
    }
  }

  /**
   * 批量将多产品挂载到同一三级分类。
   * <p>逐条处理：单条失败不影响其他产品，最终返回成功数与失败明细。</p>
   *
   * @param body  请求体，须含 {@code productIds} 列表和 {@code l3CategoryId}
   * @param actor 会话主体
   * @param scope {@code full} / {@code myCatalog}
   * @return 含 successCount/failureCount/failures 的结果
   */
  public ServiceResult batchAssociate(
      Map<String, Object> body, SessionPrincipal actor, String scope) {
    if (body == null) {
      return ServiceResult.fail(400, "ERR_VALIDATION", "请求体不能为空", null);
    }
    Object idsRaw = body.get("productIds");
    if (!(idsRaw instanceof List<?> ids) || ids.isEmpty()) {
      return ServiceResult.fail(400, "ERR_VALIDATION", "productIds 至少一项", null);
    }
    String l3Id;
    try {
      l3Id = requireL3Id(body);
      requireLeafL3(l3Id);
    } catch (BusinessException ex) {
      return ServiceResult.fail(ex.httpStatus(), ex.code(), ex.getMessage(), ex.data());
    }

    int success = 0;
    List<Map<String, Object>> failures = new ArrayList<>();
    for (Object raw : ids) {
      if (raw == null) {
        continue;
      }
      String productId = String.valueOf(raw).trim();
      if (productId.isEmpty()) {
        continue;
      }
      try {
        requireWritable(productId, actor, scope);
        String actorUserId = actor == null ? "-" : actor.userId();
        applyAssociate(productId, l3Id, actorUserId);
        success++;
      } catch (BusinessException ex) {
        Map<String, Object> fail = new LinkedHashMap<>();
        fail.put("productId", productId);
        fail.put("reasonCode", ex.code());
        fail.put("reasonMessage", ex.getMessage());
        failures.add(fail);
      }
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("successCount", success);
    result.put("failureCount", failures.size());
    result.put("failures", failures);
    return ServiceResult.ok(result);
  }

  /**
   * 按角色与 scope 判定是否可维护该产品。
   *
   * <p>PROVIDER：仅本人 create_by。ADMIN + {@code myCatalog}：仅本企业。ADMIN + full/缺省：全平台。
   * 空/缺失 create_by 在受限 scope 下 → 403。不存在 → 404。
   *
   * @param productId 产品 id 或编码
   * @param actor     会话主体
   * @param scope     维护 scope
   */
  private void requireWritable(String productId, SessionPrincipal actor, String scope) {
    Optional<CatalogProduct> existing = catalog.findProduct(productId);
    if (existing.isEmpty()) {
      throw new BusinessException(404, "404", "产品不存在", null);
    }
    Role role = actor == null ? null : actor.role();
    if (role == Role.PROVIDER) {
      String actorUserId = actor.userId();
      if (!catalog.isOwnedBy(productId, actorUserId)) {
        throw new BusinessException(403, "ERR_FORBIDDEN", "只能维护本人创建的产品", null);
      }
      return;
    }
    if (role == Role.ADMIN && isMyCatalogScope(scope)) {
      if (!enterpriseScope.isProductInEnterprise(productId, actor.enterpriseId())) {
        throw new BusinessException(403, "ERR_FORBIDDEN", "只能维护本企业产品", null);
      }
    }
  }

  /**
   * 按会话角色与 scope 选择列表源。
   *
   * <p>PROVIDER 一律本人 create_by；ADMIN {@code myCatalog} 按本企业；其余 ADMIN 全平台。
   *
   * @param actor 会话主体
   * @param scope {@code full} / {@code myCatalog} / 空
   * @return 未分页的源列表
   */
  private List<CatalogProduct> sourceProducts(SessionPrincipal actor, String scope) {
    Role role = actor == null ? null : actor.role();
    if (role == Role.PROVIDER) {
      return catalog.productsOwnedBy(actor.userId());
    }
    if (role == Role.ADMIN && isMyCatalogScope(scope)) {
      String enterpriseId = actor.enterpriseId();
      return catalog.products().stream()
          .filter(
              p -> {
                String createBy = catalog.findProductOwner(p.id()).orElse("");
                return enterpriseScope.matchesMineEnterprise(createBy, enterpriseId);
              })
          .collect(Collectors.toList());
    }
    return catalog.products();
  }

  /**
   * 是否为我的目录 scope（大小写敏感，与契约 enum 一致）。
   *
   * @param scope query scope
   * @return true 当且仅当 myCatalog
   */
  private static boolean isMyCatalogScope(String scope) {
    return scope != null && RbacMatrix.MAINTENANCE_SCOPE_MY_CATALOG.equals(scope.trim());
  }

  private CatalogProduct applyAssociate(String productId, String l3Id, String actorUserId) {
    Optional<CatalogProduct> existing = catalog.findProduct(productId);
    if (existing.isEmpty()) {
      throw new BusinessException(404, "404", "产品不存在", null);
    }
    requireLeafL3(l3Id);
    CatalogProduct src = existing.get();
    String l2 = catalog.resolveL2IdFromL3(l3Id);
    String l1 = catalog.resolveL1IdFromL3(l3Id);
    String path = catalog.resolveCategoryPathFromL3(l3Id);
    CatalogProduct updated =
        new CatalogProduct(
            src.id(),
            src.productCode(),
            src.productName(),
            src.productType(),
            l2,
            l1,
            path,
            src.chainCount(),
            src.latestVersionNo(),
            src.dataSource(),
            src.deliveryMethod(),
            src.involvesPublicData(),
            src.involvesPersonalInfo(),
            src.summary(),
            src.scenario(),
            src.supplierName(),
            src.supplierCreditCode(),
            src.tags(),
            src.typeSpecific(),
            src.businessCategory(),
            src.businessSubCategory(),
            src.updateFrequency(),
            src.billingMethod(),
            src.price(),
            src.propertyRightsType(),
            l3Id,
            src.industryCategory(),
            Instant.now());
    // 保留 create_by；刷新 update_by（actor 非空时），并为分类元数据变更生成新链版本。
    CatalogProduct saved = catalog.upsertProduct(updated, actorUserId);
    productEditorService.submitCurrentProductVersionAsync(saved);
    return saved;
  }

  private String requireL3Id(Map<String, Object> body) {
    if (body == null || body.get("l3CategoryId") == null) {
      throw new BusinessException(400, "ERR_VALIDATION", "l3CategoryId 为必填项", null);
    }
    String l3Id = String.valueOf(body.get("l3CategoryId")).trim();
    if (l3Id.isEmpty()) {
      throw new BusinessException(400, "ERR_VALIDATION", "l3CategoryId 为必填项", null);
    }
    return l3Id;
  }

  private void requireLeafL3(String l3Id) {
    Optional<CatalogCategory> cat = catalog.findCategory(l3Id);
    if (cat.isEmpty() || !"L3".equals(cat.get().level())) {
      throw new BusinessException(
          400, "ERR_CATEGORY_LEAF_REQUIRED", "产品须挂载三级分类节点", null);
    }
  }

  private static String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return "ALL";
    }
    String s = status.trim().toUpperCase(Locale.ROOT);
    if (List.of("ALL", "MAINTAINED", "PENDING").contains(s)) {
      return s;
    }
    return "ALL";
  }

  private static boolean matchesStatus(String statusNorm, CatalogProduct p) {
    boolean maintained = isMaintained(p);
    return switch (statusNorm) {
      case "MAINTAINED" -> maintained;
      case "PENDING" -> !maintained;
      default -> true;
    };
  }

  private boolean matchesL1(String l1CategoryId, CatalogProduct p) {
    if (l1CategoryId == null || l1CategoryId.isBlank()) {
      return true;
    }
    if (l1CategoryId.equals(p.l1CategoryId())) {
      return true;
    }
    if (p.l3CategoryId() == null) {
      return false;
    }
    return l1CategoryId.equals(catalog.resolveL1IdFromL3(p.l3CategoryId()));
  }

  private boolean matchesL2(String l2CategoryId, CatalogProduct p) {
    if (l2CategoryId == null || l2CategoryId.isBlank()) {
      return true;
    }
    if (l2CategoryId.equals(p.l2CategoryId())) {
      return true;
    }
    if (p.l3CategoryId() == null) {
      return false;
    }
    return l2CategoryId.equals(catalog.resolveL2IdFromL3(p.l3CategoryId()));
  }

  private static boolean matches(String filter, String value) {
    return filter == null || filter.isBlank() || filter.equals(value);
  }

  private static boolean isMaintained(CatalogProduct p) {
    return p.l3CategoryId() != null && !p.l3CategoryId().isBlank();
  }

  private Map<String, Object> toEntry(CatalogProduct p) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", p.id());
    m.put("productCode", p.productCode());
    m.put("productName", p.productName());
    m.put("maintenanceStatus", isMaintained(p) ? "MAINTAINED" : "PENDING");
    m.put("l3CategoryId", p.l3CategoryId());
    String path =
        p.l3CategoryId() == null || p.l3CategoryId().isBlank()
            ? null
            : (p.categoryPath() == null || p.categoryPath().isBlank()
                ? catalog.resolveCategoryPathFromL3(p.l3CategoryId())
                : p.categoryPath());
    m.put("categoryPath", path);
    return m;
  }

  private static CatalogProduct pendingProduct(String id, String code, String name) {
    return new CatalogProduct(
        id,
        code,
        name,
        "OTHER",
        null,
        null,
        null,
        0,
        null,
        "SELF_PRODUCED",
        "FILE",
        false,
        false,
        "待关联目录条目（演示）",
        "目录维护",
        "待关联供应商",
        "00000000000000000X",
        List.of("待关联"),
        Map.of(),
        null,
        null,
        "ON_DEMAND",
        "面议",
        "面议",
        "数据使用权",
        null,
        Instant.now());
  }
}
