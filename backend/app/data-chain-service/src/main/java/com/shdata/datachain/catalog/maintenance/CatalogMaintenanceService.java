package com.shdata.datachain.catalog.maintenance;

import com.shdata.datachain.catalog.browse.CatalogBrowseSeedStore;
import com.shdata.datachain.catalog.browse.CatalogCategory;
import com.shdata.datachain.catalog.browse.CatalogProduct;
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
 * 目录维护：列表筛选/分页、单条与批量挂载三级（REQ-CAT-007）。
 *
 * <p>维护状态：有有效 {@code l3CategoryId} → MAINTAINED，否则 PENDING。
 */
@Service
public class CatalogMaintenanceService {

  private final CatalogBrowseSeedStore catalog;

  public CatalogMaintenanceService(CatalogBrowseSeedStore catalog) {
    this.catalog = catalog;
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

  public Result list(
      String status,
      String l1CategoryId,
      String l2CategoryId,
      String l3CategoryId,
      int page,
      int pageSize) {
    String statusNorm = normalizeStatus(status);
    int safePage = Math.max(page, 1);
    int safeSize = Math.min(Math.max(pageSize, 1), 100);

    List<CatalogProduct> filtered =
        catalog.products().stream()
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
    return Result.ok(pageBody);
  }

  public Result associate(String productId, Map<String, Object> body) {
    try {
      String l3Id = requireL3Id(body);
      CatalogProduct updated = applyAssociate(productId, l3Id);
      return Result.ok(toEntry(updated));
    } catch (BusinessException ex) {
      return Result.fail(ex.httpStatus, ex.code, ex.message, ex.data);
    }
  }

  public Result batchAssociate(Map<String, Object> body) {
    if (body == null) {
      return Result.fail(400, "ERR_VALIDATION", "请求体不能为空", null);
    }
    Object idsRaw = body.get("productIds");
    if (!(idsRaw instanceof List<?> ids) || ids.isEmpty()) {
      return Result.fail(400, "ERR_VALIDATION", "productIds 至少一项", null);
    }
    String l3Id;
    try {
      l3Id = requireL3Id(body);
      requireLeafL3(l3Id);
    } catch (BusinessException ex) {
      return Result.fail(ex.httpStatus, ex.code, ex.message, ex.data);
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
        applyAssociate(productId, l3Id);
        success++;
      } catch (BusinessException ex) {
        Map<String, Object> fail = new LinkedHashMap<>();
        fail.put("productId", productId);
        fail.put("reasonCode", ex.code);
        fail.put("reasonMessage", ex.message);
        failures.add(fail);
      }
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("successCount", success);
    result.put("failureCount", failures.size());
    result.put("failures", failures);
    return Result.ok(result);
  }

  private CatalogProduct applyAssociate(String productId, String l3Id) {
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
            Instant.now());
    catalog.upsertProduct(updated);
    return updated;
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

  static final class BusinessException extends RuntimeException {
    final int httpStatus;
    final String code;
    final String message;
    final Map<String, Object> data;

    BusinessException(int httpStatus, String code, String message, Map<String, Object> data) {
      super(message);
      this.httpStatus = httpStatus;
      this.code = code;
      this.message = message;
      this.data = data;
    }
  }

  public record Result(boolean ok, int httpStatus, String code, String message, Object data) {
    static Result ok(Object data) {
      return new Result(true, 200, "0", "ok", data);
    }

    static Result fail(int httpStatus, String code, String message, Object data) {
      return new Result(false, httpStatus, code, message, data);
    }
  }
}
