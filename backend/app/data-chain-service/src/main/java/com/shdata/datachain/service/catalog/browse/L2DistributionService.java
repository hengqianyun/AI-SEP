package com.shdata.datachain.service.catalog.browse;

import com.shdata.datachain.model.CatalogProduct;
import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * L2 产品分布服务（座序图；REQ-CAT-009 / TASK-WSC-604；REQ-CAT-015 / TASK-WSC-902）。
 *
 * <p>Tips：
 * <ul>
 *   <li>{@link #l2Distribution()} — 全平台真实产品总数 + 按 count 降序的 L2 items</li>
 *   <li>{@link #l2Distribution(String)} — 可选 {@code l1CategoryId} 限定 L1 子树；全链无企业 scope</li>
 * </ul>
 */
@Service
public class L2DistributionService {

  private final CatalogBrowseSeedStore store;

  public L2DistributionService(CatalogBrowseSeedStore store) {
    this.store = store;
  }

  /**
   * 查询二级目录产品分布（全平台）。
   *
   * @return OpenAPI {@code L2Distribution} 形态的 Map
   */
  public Map<String, Object> l2Distribution() {
    return l2Distribution(null);
  }

  /**
   * 查询二级目录产品分布，可选按一级分类过滤。
   *
   * <p>{@code totalProducts} 为匹配 L1（或全平台）的产品真实总数；
   * {@code items} 按 count 降序。空目录返回总数 0 与空列表。
   *
   * @param l1CategoryId 可选一级分类 id；null/blank 等同全平台
   * @return OpenAPI {@code L2Distribution} 形态的 Map
   */
  public Map<String, Object> l2Distribution(String l1CategoryId) {
    if (l1CategoryId == null || l1CategoryId.isBlank()) {
      return store.l2Distribution();
    }
    return l2DistributionForL1(l1CategoryId.trim());
  }

  /**
   * 按 L1 子树聚合 L2 分布与产品总数。
   *
   * @param l1CategoryId 一级分类 id
   * @return L2Distribution 形态的 Map
   */
  private Map<String, Object> l2DistributionForL1(String l1CategoryId) {
    List<CatalogProduct> filtered =
        store.products().stream()
            .filter(p -> l1CategoryId.equals(p.l1CategoryId()))
            .toList();

    Map<String, Long> counts = new LinkedHashMap<>();
    for (CatalogProduct product : filtered) {
      String l2Code = product.l2CategoryId();
      if (l2Code == null || l2Code.isBlank()) {
        continue;
      }
      counts.merge(l2Code, 1L, Long::sum);
    }

    List<Map<String, Object>> items =
        counts.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .map(
                entry -> {
                  Map<String, Object> row = new LinkedHashMap<>();
                  row.put("code", entry.getKey());
                  row.put(
                      "name",
                      store
                          .findCategory(entry.getKey())
                          .map(category -> category.name())
                          .orElse(entry.getKey()));
                  row.put("count", entry.getValue().intValue());
                  return row;
                })
            .toList();

    Map<String, Object> data = new LinkedHashMap<>();
    data.put("totalProducts", filtered.size());
    data.put("items", items);
    return data;
  }
}
