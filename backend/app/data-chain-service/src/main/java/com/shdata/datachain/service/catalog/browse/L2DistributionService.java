package com.shdata.datachain.service.catalog.browse;

import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * L2 产品分布服务（座序图；REQ-CAT-009 / TASK-WSC-604）。
 *
 * <p>Tips：
 * <ul>
 *   <li>{@link #l2Distribution()} — 真实产品总数 + 按 count 降序的 L2 items（消费方可取 Top5）</li>
 * </ul>
 */
@Service
public class L2DistributionService {

  private final CatalogBrowseSeedStore store;

  public L2DistributionService(CatalogBrowseSeedStore store) {
    this.store = store;
  }

  /**
   * 查询二级目录产品分布。
   *
   * <p>{@code totalProducts} 为库内产品真实总数（不得用 Top5 之和冒充）；{@code items} 按 count
   * 降序，含 code/name/count。空目录返回总数 0 与空列表。
   *
   * @return OpenAPI {@code L2Distribution} 形态的 Map
   */
  public Map<String, Object> l2Distribution() {
    // 委托 CatalogBrowseSeedStore：按 l2_category_code 计数并降序
    return store.l2Distribution();
  }
}
