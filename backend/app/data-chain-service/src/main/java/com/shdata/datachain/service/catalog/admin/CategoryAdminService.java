package com.shdata.datachain.service.catalog.admin;

import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import com.shdata.datachain.model.CatalogCategory;
import com.shdata.datachain.common.exception.BusinessException;
import com.shdata.datachain.common.response.ServiceResult;
import com.shdata.datachain.common.security.AuthAuditLogger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.shdata.datachain.model.CatalogCategory;
import com.shdata.datachain.repository.CatalogBrowseSeedStore;

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

  /**
   * 新建分类（空间 L1 / 行业 L2 / 子类 L3）。
   * <p>校验 name 非空、level 为 L1/L2/L3、parentId 层级关系正确。</p>
   *
   * @param body 含 name、level、parentId（L2/L3 必填）
   * @return 创建后的分类
   */
  public ServiceResult create(Map<String, Object> body) {
    try {
      Validated v = validate(body, null);
      String id = catalog.nextCategoryId();
      CatalogCategory created = new CatalogCategory(id, v.name(), v.level(), v.parentId());
      catalog.addCategory(created);
      return ServiceResult.ok(toMap(created));
    } catch (BusinessException ex) {
      return ServiceResult.fail(ex.httpStatus(), ex.code(), ex.getMessage(), ex.data());
    }
  }

  /**
   * 编辑分类名称。禁止变更层级（level），仅可改 name。
   *
   * @param categoryId 分类 ID
   * @param body       含 name、level（须与原层级一致）
   * @return 更新后的分类
   */
  public ServiceResult update(String categoryId, Map<String, Object> body) {
    Optional<CatalogCategory> existing = catalog.findCategory(categoryId);
    if (existing.isEmpty()) {
      return ServiceResult.fail(404, "404", "分类不存在", null);
    }
    try {
      Validated v = validate(body, categoryId);
      if (!existing.get().level().equals(v.level())) {
        throw new BusinessException(400, "ERR_VALIDATION", "禁止变更分类层级", null);
      }
      CatalogCategory updated = new CatalogCategory(categoryId, v.name(), v.level(), v.parentId());
      catalog.replaceCategory(updated);
      return ServiceResult.ok(toMap(updated));
    } catch (BusinessException ex) {
      return ServiceResult.fail(ex.httpStatus(), ex.code(), ex.getMessage(), ex.data());
    }
  }

  /**
   * 删除分类。需满足：无产品挂载、无子分类、非最后 L3。
   *
   * @param categoryId    分类 ID
   * @param actorUserId   操作人用户 ID
   * @param correlationId 请求追踪 ID
   * @return 成功返回 null data，失败返回原因
   */
  public ServiceResult delete(String categoryId, String actorUserId, String correlationId) {
    Optional<CatalogCategory> existing = catalog.findCategory(categoryId);
    if (existing.isEmpty()) {
      return ServiceResult.fail(404, "404", "分类不存在", null);
    }
    CatalogCategory category = existing.get();

    int mounted = catalog.countMountedProducts(category);
    if (mounted > 0) {
      Map<String, Object> data = new LinkedHashMap<>();
      data.put("reason", "分类下仍有 " + mounted + " 个产品");
      data.put("productCount", mounted);
      auditLogger.categoryDeleteRejected(actorUserId, categoryId, mounted, correlationId);
      return ServiceResult.fail(
          409, "ERR_CATEGORY_HAS_PRODUCTS", "分类下仍有挂载产品，禁止删除", data);
    }

    if ("L3".equals(category.level()) && countCategoriesByLevel("L3") <= 1) {
      Map<String, Object> data = new LinkedHashMap<>();
      data.put("reason", "须至少保留一个三级分类");
      data.put("productCount", 0);
      return ServiceResult.fail(409, "ERR_CATEGORY_HAS_PRODUCTS", "须至少保留一个三级分类，禁止删除", data);
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
      return ServiceResult.fail(409, "ERR_CATEGORY_HAS_PRODUCTS", "分类下仍有子分类，禁止删除", data);
    }

    catalog.removeCategory(categoryId);
    return ServiceResult.ok(null);
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

}
