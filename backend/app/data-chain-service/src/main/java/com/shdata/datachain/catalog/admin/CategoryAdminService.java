package com.shdata.datachain.catalog.admin;

import com.shdata.datachain.catalog.browse.CatalogBrowseSeedStore;
import com.shdata.datachain.catalog.browse.CatalogCategory;
import com.shdata.datachain.security.AuthAuditLogger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * 三级分类维护（空间 L1 / 行业 L2 / 子类 L3）— REQ-CAT-006 / DEC-WSC-003。
 *
 * <p>挂载计数委托 {@link CatalogBrowseSeedStore#countMountedProducts}（含 L3）。
 */
@Service
public class CategoryAdminService {

  private final CatalogBrowseSeedStore catalog;
  private final AuthAuditLogger auditLogger;

  public CategoryAdminService(CatalogBrowseSeedStore catalog, AuthAuditLogger auditLogger) {
    this.catalog = catalog;
    this.auditLogger = auditLogger;
  }

  public Result create(Map<String, Object> body) {
    try {
      Validated v = validate(body, null);
      String id = catalog.nextCategoryId();
      CatalogCategory created = new CatalogCategory(id, v.name(), v.level(), v.parentId());
      catalog.addCategory(created);
      return Result.ok(toMap(created));
    } catch (BusinessException ex) {
      return Result.fail(ex.httpStatus, ex.code, ex.message, ex.data);
    }
  }

  public Result update(String categoryId, Map<String, Object> body) {
    Optional<CatalogCategory> existing = catalog.findCategory(categoryId);
    if (existing.isEmpty()) {
      return Result.fail(404, "404", "分类不存在", null);
    }
    try {
      Validated v = validate(body, categoryId);
      if (!existing.get().level().equals(v.level())) {
        throw new BusinessException(400, "ERR_VALIDATION", "禁止变更分类层级", null);
      }
      CatalogCategory updated = new CatalogCategory(categoryId, v.name(), v.level(), v.parentId());
      catalog.replaceCategory(updated);
      return Result.ok(toMap(updated));
    } catch (BusinessException ex) {
      return Result.fail(ex.httpStatus, ex.code, ex.message, ex.data);
    }
  }

  public Result delete(String categoryId, String actorUserId, String correlationId) {
    Optional<CatalogCategory> existing = catalog.findCategory(categoryId);
    if (existing.isEmpty()) {
      return Result.fail(404, "404", "分类不存在", null);
    }
    CatalogCategory category = existing.get();

    int mounted = catalog.countMountedProducts(category);
    if (mounted > 0) {
      Map<String, Object> data = new LinkedHashMap<>();
      data.put("reason", "分类下仍有 " + mounted + " 个产品");
      data.put("productCount", mounted);
      auditLogger.categoryDeleteRejected(actorUserId, categoryId, mounted, correlationId);
      return Result.fail(
          409, "ERR_CATEGORY_HAS_PRODUCTS", "分类下仍有挂载产品，禁止删除", data);
    }

    if ("L3".equals(category.level()) && countCategoriesByLevel("L3") <= 1) {
      Map<String, Object> data = new LinkedHashMap<>();
      data.put("reason", "须至少保留一个三级分类");
      data.put("productCount", 0);
      return Result.fail(409, "ERR_CATEGORY_HAS_PRODUCTS", "须至少保留一个三级分类，禁止删除", data);
    }

    long childCount = countDirectChildren(category);
    if (childCount > 0) {
      String childLabel =
          "L1".equals(category.level())
              ? "个二级分类"
              : "L2".equals(category.level()) ? "个三级分类" : "个子节点";
      Map<String, Object> data = new LinkedHashMap<>();
      data.put("reason", "分类下仍有 " + childCount + " " + childLabel);
      data.put("productCount", 0);
      return Result.fail(409, "ERR_CATEGORY_HAS_PRODUCTS", "分类下仍有子分类，禁止删除", data);
    }

    catalog.removeCategory(categoryId);
    return Result.ok(null);
  }

  private long countDirectChildren(CatalogCategory category) {
    String childLevel =
        "L1".equals(category.level()) ? "L2" : "L2".equals(category.level()) ? "L3" : null;
    if (childLevel == null) {
      return 0;
    }
    String id = category.id();
    return catalog.categories().stream()
        .filter(c -> childLevel.equals(c.level()) && id.equals(c.parentId()))
        .count();
  }

  private long countCategoriesByLevel(String level) {
    return catalog.categories().stream().filter(c -> level.equals(c.level())).count();
  }

  private Validated validate(Map<String, Object> body, String updatingId) {
    if (body == null) {
      throw new BusinessException(400, "ERR_VALIDATION", "请求体不能为空", null);
    }
    String name = body.get("name") == null ? "" : String.valueOf(body.get("name")).trim();
    String level = body.get("level") == null ? "" : String.valueOf(body.get("level")).trim();
    String parentId =
        body.get("parentId") == null || String.valueOf(body.get("parentId")).isBlank()
            ? null
            : String.valueOf(body.get("parentId")).trim();
    if (name.isEmpty()) {
      throw new BusinessException(400, "ERR_VALIDATION", "分类名称为必填项", null);
    }
    if (!List.of("L1", "L2", "L3").contains(level)) {
      throw new BusinessException(400, "ERR_VALIDATION", "level 须为 L1、L2 或 L3", null);
    }
    if ("L1".equals(level)) {
      if (parentId != null) {
        throw new BusinessException(400, "ERR_VALIDATION", "一级（空间）不得指定 parentId", null);
      }
    } else if ("L2".equals(level)) {
      if (parentId == null) {
        throw new BusinessException(400, "ERR_VALIDATION", "二级（行业）须指定 parentId", null);
      }
      Optional<CatalogCategory> parent = catalog.findCategory(parentId);
      if (parent.isEmpty() || !"L1".equals(parent.get().level())) {
        throw new BusinessException(400, "ERR_VALIDATION", "parentId 须为已存在的一级（空间）", null);
      }
    } else {
      if (parentId == null) {
        throw new BusinessException(400, "ERR_VALIDATION", "三级（子类）须指定 parentId", null);
      }
      Optional<CatalogCategory> parent = catalog.findCategory(parentId);
      if (parent.isEmpty() || !"L2".equals(parent.get().level())) {
        throw new BusinessException(400, "ERR_VALIDATION", "parentId 须为已存在的二级（行业）", null);
      }
    }
    if (updatingId != null && updatingId.equals(parentId)) {
      throw new BusinessException(400, "ERR_VALIDATION", "parentId 不得为自身", null);
    }
    return new Validated(name, level, parentId);
  }

  private static Map<String, Object> toMap(CatalogCategory c) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", c.id());
    m.put("name", c.name());
    m.put("level", c.level());
    m.put("parentId", c.parentId());
    return m;
  }

  private record Validated(String name, String level, String parentId) {}

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
