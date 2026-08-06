package com.shdata.datachain.service.catalog.browse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.shdata.datachain.model.CatalogCategory;
import com.shdata.datachain.model.CatalogProduct;
import com.shdata.datachain.repository.CatalogBrowseSeedStore;

/**
 * 目录浏览只读服务，提供三级分类树和产品的列表/详情/筛选/分页查询。
 */
@Service
public class CatalogBrowseService {

  private final CatalogBrowseSeedStore store;

  public CatalogBrowseService(CatalogBrowseSeedStore store) {
    this.store = store;
  }

  /**
   * 列出全部分类（L1/L2/L3），含完整路径标签。
   *
   * @return 分类列表
   */
  public List<Map<String, Object>> listCategories() {
    return store.categories().stream().map(this::categoryToMap).collect(Collectors.toList());
  }

  /**
   * 按 ID 查询产品详情。
   *
   * @param id 产品 ID
   * @return 产品数据，不存在返回 empty
   */
  public Optional<Map<String, Object>> getProduct(String id) {
    return store.findProduct(id).map(this::productToMap);
  }

  /**
   * 多条件筛选分页查询产品列表。
   * <p>筛选维度：一级/二级/三级分类、行业门类（GB/T）、数据来源、产品类型、是否涉公、交付方式、关键字。
   * 按 {@code updatedAt} 降序排序，分页参数自动裁剪（pageSize ≤ 100）。</p>
   *
   * @param l1CategoryId       一级分类 ID（可选）
   * @param l2CategoryId       二级分类 ID（可选，会联动匹配三级）
   * @param l3CategoryId       三级分类 ID（可选）
   * @param industryCategory   GB/T 4754 门类中文原文（可选；对齐产品字段）
   * @param dataSource         数据来源（可选）
   * @param productType        产品类型（可选）
   * @param involvesPublicData 是否涉公（可选）
   * @param deliveryMethod     交付方式（可选）
   * @param q                  关键字搜索（匹配产品名/编码）
   * @param page               页码（1-based）
   * @param pageSize           每页条数（≤100）
   * @return 分页结果，含 items/page/pageSize/total
   */
  public Map<String, Object> listProducts(
      String l1CategoryId,
      String l2CategoryId,
      String l3CategoryId,
      String industryCategory,
      String dataSource,
      String productType,
      Boolean involvesPublicData,
      String deliveryMethod,
      String q,
      int page,
      int pageSize) {
    return listProducts(
        l1CategoryId,
        l2CategoryId,
        l3CategoryId,
        industryCategory,
        dataSource,
        productType,
        involvesPublicData,
        deliveryMethod,
        q,
        null,
        page,
        pageSize);
  }

  /**
   * @param ownerUserId 非空时仅返回 create_by 匹配的产品（我的数据产品）
   */
  public Map<String, Object> listProducts(
      String l1CategoryId,
      String l2CategoryId,
      String l3CategoryId,
      String industryCategory,
      String dataSource,
      String productType,
      Boolean involvesPublicData,
      String deliveryMethod,
      String q,
      String ownerUserId,
      int page,
      int pageSize) {
    int safePage = Math.max(page, 1);
    int safeSize = Math.min(Math.max(pageSize, 1), 100);
    String keyword = q == null ? null : q.trim().toLowerCase(Locale.ROOT);

    List<CatalogProduct> source =
        ownerUserId != null && !ownerUserId.isBlank()
            ? store.productsOwnedBy(ownerUserId)
            : store.products();

    List<CatalogProduct> filtered =
        source.stream()
            .filter(p -> matches(l1CategoryId, p.l1CategoryId()))
            .filter(p -> matchesL2(l2CategoryId, p))
            .filter(p -> matches(l3CategoryId, p.l3CategoryId()))
            .filter(p -> matches(industryCategory, p.industryCategory()))
            .filter(p -> matches(dataSource, p.dataSource()))
            .filter(p -> matches(productType, p.productType()))
            .filter(p -> matches(deliveryMethod, p.deliveryMethod()))
            .filter(
                p ->
                    involvesPublicData == null
                        || involvesPublicData.equals(p.involvesPublicData()))
            .filter(p -> matchesKeyword(keyword, p))
            .sorted(
                Comparator.comparing(
                        (CatalogProduct p) -> p.updatedAt() != null ? p.updatedAt() : Instant.EPOCH)
                    .reversed())
            .collect(Collectors.toList());

    long total = filtered.size();
    int from = Math.min((safePage - 1) * safeSize, filtered.size());
    int to = Math.min(from + safeSize, filtered.size());
    List<Map<String, Object>> items =
        filtered.subList(from, to).stream().map(this::productToMap).collect(Collectors.toList());

    Map<String, Object> pageBody = new LinkedHashMap<>();
    pageBody.put("items", items);
    pageBody.put("page", safePage);
    pageBody.put("pageSize", safeSize);
    pageBody.put("total", total);
    return pageBody;
  }

  /** 二级分类匹配：含联动逻辑 — 用户传 l2CategoryId 时也匹配其下 L3 子分类下的产品。 */
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
    return store
        .findCategory(p.l3CategoryId())
        .map(c -> l2CategoryId.equals(c.parentId()))
        .orElse(false);
  }

  /** 字符串精确匹配（null/blank 表示忽略该过滤条件）。 */
  private static boolean matches(String filter, String value) {
    return filter == null || filter.isBlank() || filter.equals(value);
  }

  /** 关键字匹配产品名称或编码（大小写不敏感）。 */
  private static boolean matchesKeyword(String keywordLower, CatalogProduct p) {
    if (keywordLower == null || keywordLower.isEmpty()) {
      return true;
    }
    String name = p.productName() == null ? "" : p.productName().toLowerCase(Locale.ROOT);
    String code = p.productCode() == null ? "" : p.productCode().toLowerCase(Locale.ROOT);
    return name.contains(keywordLower) || code.contains(keywordLower);
  }

  /** 分类转 API 视图（含路径标签）。 */
  private Map<String, Object> categoryToMap(CatalogCategory c) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", c.id());
    m.put("name", c.name());
    m.put("level", c.level());
    m.put("parentId", c.parentId());
    m.put("pathLabels", store.resolvePathLabels(c.id()));
    return m;
  }

  /** 产品转 API 视图。 */
  private Map<String, Object> productToMap(CatalogProduct p) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", p.id());
    m.put("productCode", p.productCode());
    m.put("productName", p.productName());
    m.put("productType", p.productType());
    if (p.industryCategory() != null) {
      m.put("industryCategory", p.industryCategory());
    }
    m.put("l2CategoryId", p.l2CategoryId());
    if (p.l3CategoryId() != null) {
      m.put("l3CategoryId", p.l3CategoryId());
    }
    m.put("categoryPath", p.categoryPath());
    m.put("chainCount", p.chainCount());
    m.put("latestVersionNo", p.latestVersionNo());
    m.put("dataSource", p.dataSource());
    m.put("deliveryMethod", p.deliveryMethod());
    m.put("involvesPublicData", p.involvesPublicData());
    m.put("involvesPersonalInfo", p.involvesPersonalInfo());
    m.put("summary", p.summary());
    m.put("scenario", p.scenario());
    m.put("supplierName", p.supplierName());
    m.put("supplierCreditCode", p.supplierCreditCode());
    m.put("tags", p.tags() == null ? List.of() : new ArrayList<>(p.tags()));
    m.put("businessCategory", p.businessCategory());
    m.put("businessSubCategory", p.businessSubCategory());
    m.put("updateFrequency", p.updateFrequency());
    m.put("billingMethod", p.billingMethod());
    m.put("price", p.price());
    m.put("propertyRightsType", p.propertyRightsType());
    if (p.typeSpecific() != null && !p.typeSpecific().isEmpty()) {
      m.put("typeSpecific", new LinkedHashMap<>(p.typeSpecific()));
    }
    if (p.updatedAt() != null) {
      m.put("updatedAt", p.updatedAt().toString());
    }
    return m;
  }
}
