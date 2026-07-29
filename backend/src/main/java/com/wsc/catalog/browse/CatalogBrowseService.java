package com.wsc.catalog.browse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CatalogBrowseService {

  private final CatalogBrowseSeedStore store;

  public CatalogBrowseService(CatalogBrowseSeedStore store) {
    this.store = store;
  }

  public List<Map<String, Object>> listCategories() {
    return store.categories().stream().map(this::categoryToMap).collect(Collectors.toList());
  }

  public Optional<Map<String, Object>> getProduct(String id) {
    return store.findProduct(id).map(this::productToMap);
  }

  public Map<String, Object> listProducts(
      String l1CategoryId,
      String l2CategoryId,
      String dataSource,
      String productType,
      Boolean involvesPublicData,
      String deliveryMethod,
      String q,
      int page,
      int pageSize) {
    int safePage = Math.max(page, 1);
    int safeSize = Math.min(Math.max(pageSize, 1), 100);
    String keyword = q == null ? null : q.trim().toLowerCase(Locale.ROOT);

    List<CatalogProduct> filtered =
        store.products().stream()
            .filter(p -> matches(l1CategoryId, p.l1CategoryId()))
            .filter(p -> matches(l2CategoryId, p.l2CategoryId()))
            .filter(p -> matches(dataSource, p.dataSource()))
            .filter(p -> matches(productType, p.productType()))
            .filter(p -> matches(deliveryMethod, p.deliveryMethod()))
            .filter(
                p ->
                    involvesPublicData == null
                        || involvesPublicData.equals(p.involvesPublicData()))
            .filter(p -> matchesKeyword(keyword, p))
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

  private static boolean matches(String filter, String value) {
    return filter == null || filter.isBlank() || filter.equals(value);
  }

  private static boolean matchesKeyword(String keywordLower, CatalogProduct p) {
    if (keywordLower == null || keywordLower.isEmpty()) {
      return true;
    }
    String name = p.productName() == null ? "" : p.productName().toLowerCase(Locale.ROOT);
    String code = p.productCode() == null ? "" : p.productCode().toLowerCase(Locale.ROOT);
    return name.contains(keywordLower) || code.contains(keywordLower);
  }

  private Map<String, Object> categoryToMap(CatalogCategory c) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", c.id());
    m.put("name", c.name());
    m.put("level", c.level());
    m.put("parentId", c.parentId());
    return m;
  }

  private Map<String, Object> productToMap(CatalogProduct p) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", p.id());
    m.put("productCode", p.productCode());
    m.put("productName", p.productName());
    m.put("productType", p.productType());
    m.put("l2CategoryId", p.l2CategoryId());
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
    if (p.typeSpecific() != null && !p.typeSpecific().isEmpty()) {
      m.put("typeSpecific", new LinkedHashMap<>(p.typeSpecific()));
    }
    return m;
  }
}
