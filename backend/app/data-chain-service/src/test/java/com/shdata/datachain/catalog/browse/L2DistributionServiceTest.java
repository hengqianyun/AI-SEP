package com.shdata.datachain.catalog.browse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import com.shdata.datachain.service.catalog.browse.L2DistributionService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * L2DistributionService 单元测（TASK-WSC-604）：委托透传；items 降序；空目录安全。
 */
class L2DistributionServiceTest {

  private CatalogBrowseSeedStore store;
  private L2DistributionService service;

  @BeforeEach
  void setUp() {
    store = mock(CatalogBrowseSeedStore.class);
    service = new L2DistributionService(store);
  }

  @Test
  void l2Distribution_passesThroughTotalProductsAndItems() {
    Map<String, Object> seed = new LinkedHashMap<>();
    seed.put("totalProducts", 10);
    seed.put(
        "items",
        List.of(
            item("cat-l2-emr", "电子病历", 5),
            item("cat-l2-imaging", "医学影像", 2),
            item("cat-l2-credit", "征信评估", 1)));
    when(store.l2Distribution()).thenReturn(seed);

    Map<String, Object> data = service.l2Distribution();
    assertEquals(10, data.get("totalProducts"));
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
    int itemsSum = items.stream().mapToInt(r -> ((Number) r.get("count")).intValue()).sum();
    assertTrue(
        ((Number) data.get("totalProducts")).intValue() > itemsSum,
        "contract: totalProducts is real size (may exceed L2-tagged items sum)");
  }

  @Test
  void l2Distribution_itemsSortedByCountDesc() {
    Map<String, Object> seed = new LinkedHashMap<>();
    seed.put("totalProducts", 6);
    seed.put(
        "items",
        List.of(
            item("a", "A", 3),
            item("b", "B", 2),
            item("c", "C", 1)));
    when(store.l2Distribution()).thenReturn(seed);

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> items =
        (List<Map<String, Object>>) service.l2Distribution().get("items");
    assertEquals(3, ((Number) items.get(0).get("count")).intValue());
    assertEquals(2, ((Number) items.get(1).get("count")).intValue());
    assertEquals(1, ((Number) items.get(2).get("count")).intValue());
  }

  @Test
  void l2Distribution_emptyCatalog_safe() {
    Map<String, Object> seed = new LinkedHashMap<>();
    seed.put("totalProducts", 0);
    seed.put("items", List.of());
    when(store.l2Distribution()).thenReturn(seed);

    Map<String, Object> data = service.l2Distribution();
    assertEquals(0, data.get("totalProducts"));
    assertTrue(((List<?>) data.get("items")).isEmpty());
  }

  private static Map<String, Object> item(String code, String name, int count) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("code", code);
    row.put("name", name);
    row.put("count", count);
    return row;
  }
}
