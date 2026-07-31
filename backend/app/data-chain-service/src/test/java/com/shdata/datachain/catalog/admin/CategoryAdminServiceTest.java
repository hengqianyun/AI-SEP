package com.shdata.datachain.catalog.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.shdata.datachain.catalog.browse.CatalogBrowseSeedStore;
import com.shdata.datachain.catalog.browse.CatalogCategory;
import com.shdata.datachain.security.AuthAuditLogger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** REQ-CAT-006 服务单测。 */
class CategoryAdminServiceTest {

  private CatalogBrowseSeedStore catalog;
  private CategoryAdminService service;

  @BeforeEach
  void setUp() {
    catalog = new CatalogBrowseSeedStore();
    service = new CategoryAdminService(catalog, mock(AuthAuditLogger.class));
  }

  @Test
  void createL3_underL2_and_deleteExtraEmptyL3_ok() {
    String l1 = create("空间A", "L1", null);
    String l2 = create("行业A", "L2", l1);
    String l3a = create("子类A", "L3", l2);
    String l3b = create("子类B", "L3", l2);

    CategoryAdminService.Result ok = service.delete(l3b, "admin", "corr-ok");
    assertTrue(ok.ok());
    assertTrue(catalog.findCategory(l3a).isPresent());
  }

  @Test
  void deleteL2_withL3Child_forbidden() {
    String l1 = create("空间B", "L1", null);
    String l2 = create("行业B", "L2", l1);
    create("子类", "L3", l2);

    CategoryAdminService.Result deny = service.delete(l2, "admin", "corr-child");
    assertFalse(deny.ok());
    assertEquals(409, deny.httpStatus());
    assertEquals("ERR_CATEGORY_HAS_PRODUCTS", deny.code());
  }

  @Test
  void deleteMountedL3_forbidden() {
    CategoryAdminService.Result deny =
        service.delete("cat-l3-emr-desense", "admin", "corr-mounted");
    assertFalse(deny.ok());
    assertEquals(409, deny.httpStatus());
    assertEquals("ERR_CATEGORY_HAS_PRODUCTS", deny.code());
    assertTrue(String.valueOf(deny.data()).contains("产品"));
  }

  @Test
  void lastL3_globally_forbidden() {
    CatalogBrowseSeedStore mockStore = mock(CatalogBrowseSeedStore.class);
    CatalogCategory only = new CatalogCategory("l3-only", "独苗子类", "L3", "l2-x");
    when(mockStore.findCategory("l3-only")).thenReturn(Optional.of(only));
    when(mockStore.categories()).thenReturn(List.of(only));
    when(mockStore.countMountedProducts(any())).thenReturn(0);

    CategoryAdminService isolated =
        new CategoryAdminService(mockStore, mock(AuthAuditLogger.class));
    CategoryAdminService.Result deny = isolated.delete("l3-only", "admin", "corr-last");
    assertFalse(deny.ok());
    assertEquals(409, deny.httpStatus());
    assertEquals("ERR_CATEGORY_HAS_PRODUCTS", deny.code());
    assertTrue(deny.message().contains("至少保留一个三级"));
  }

  @Test
  void rejectInvalidL3Parent() {
    CategoryAdminService.Result bad = service.create(body("坏子类", "L3", "cat-l1-health"));
    assertFalse(bad.ok());
    assertEquals(400, bad.httpStatus());
  }

  private String create(String name, String level, String parentId) {
    CategoryAdminService.Result r = service.create(body(name, level, parentId));
    assertTrue(r.ok(), () -> "create failed: " + r.code() + " " + r.message());
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) r.data();
    return String.valueOf(data.get("id"));
  }

  private static Map<String, Object> body(String name, String level, String parentId) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("name", name);
    m.put("level", level);
    if (parentId != null) {
      m.put("parentId", parentId);
    }
    return m;
  }
}
