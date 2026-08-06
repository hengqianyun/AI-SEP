package com.shdata.datachain.catalog.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import com.shdata.datachain.model.CatalogCategory;
import com.shdata.datachain.common.response.ServiceResult;
import com.shdata.datachain.entity.DataProductEntity;
import com.shdata.datachain.entity.IndustryCategoryEntity;
import com.shdata.datachain.repository.DataProductRepository;
import com.shdata.datachain.repository.IndustryCategoryRepository;
import com.shdata.datachain.common.security.AuthAuditLogger;
import com.shdata.datachain.service.catalog.admin.CategoryAdminService;
import java.util.ArrayList;
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
  private IndustryCategoryRepository categoryRepo;
  private DataProductRepository productRepo;
  private List<IndustryCategoryEntity> categoryList;
  private List<DataProductEntity> productList;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    categoryRepo = mock(IndustryCategoryRepository.class);
    productRepo = mock(DataProductRepository.class);

    categoryList = new ArrayList<>();
    productList = new ArrayList<>();

    when(categoryRepo.findAll()).thenReturn(categoryList);
    when(categoryRepo.save(any())).thenAnswer(inv -> {
      IndustryCategoryEntity e = inv.getArgument(0);
      if (e.getId() == null) e.setId((long) (categoryList.size() + 1));
      categoryList.removeIf(c -> c.getCode().equals(e.getCode()));
      categoryList.add(e);
      return e;
    });
    when(productRepo.findAll()).thenReturn(productList);
    when(productRepo.save(any())).thenAnswer(inv -> {
      DataProductEntity e = inv.getArgument(0);
      if (e.getId() == null) e.setId((long) (productList.size() + 1));
      productList.removeIf(p -> p.getProductCode().equals(e.getProductCode()));
      productList.add(e);
      return e;
    });
    when(productRepo.count()).thenReturn((long) productList.size());

    catalog = new CatalogBrowseSeedStore(categoryRepo, productRepo);
    // 种子分类（与旧内存 seed 一致）
    seedCategory("cat-l1-health", "医疗卫生", "L1", null);
    seedCategory("cat-l1-finance", "金融服务", "L1", null);
    seedCategory("cat-l2-emr", "电子病历", "L2", "cat-l1-health");
    seedCategory("cat-l2-imaging", "医学影像", "L2", "cat-l1-health");
    seedCategory("cat-l2-credit", "征信评估", "L2", "cat-l1-finance");
    seedCategory("cat-l2-txn", "交易流水", "L2", "cat-l1-finance");
    seedCategory("cat-l3-emr-desense", "脱敏病历", "L3", "cat-l2-emr");
    seedCategory("cat-l3-emr-struct", "结构化病历", "L3", "cat-l2-emr");
    seedCategory("cat-l3-img-ct", "CT 影像", "L3", "cat-l2-imaging");
    seedCategory("cat-l3-credit-score", "征信评分", "L3", "cat-l2-credit");
    seedCategory("cat-l3-txn-retail", "零售支付", "L3", "cat-l2-txn");
    // 种子产品（有三级分类挂载）
    seedProduct("MED-EMR-0001", "cat-l3-emr-desense", "cat-l1-health", "cat-l2-emr");

    service = new CategoryAdminService(catalog, mock(AuthAuditLogger.class));
  }

  @Test
  void createL3_underL2_and_deleteExtraEmptyL3_ok() {
    String l3b = create("子类B", "L3", "cat-l2-emr");
    ServiceResult ok = service.delete(l3b, "admin", "corr-ok");
    assertTrue(ok.ok());
  }

  @Test
  void deleteL2_withL3Child_forbidden() {
    String l2 = create("行业B", "L2", "cat-l1-health");
    create("子类", "L3", l2);
    ServiceResult deny = service.delete(l2, "admin", "corr-child");
    assertFalse(deny.ok());
    assertEquals(409, deny.httpStatus());
    assertEquals("ERR_CATEGORY_HAS_PRODUCTS", deny.code());
  }

  @Test
  void deleteMountedL3_forbidden() {
    ServiceResult deny = service.delete("cat-l3-emr-desense", "admin", "corr-mounted");
    assertFalse(deny.ok());
    assertEquals(409, deny.httpStatus());
    assertEquals("ERR_CATEGORY_HAS_PRODUCTS", deny.code());
  }

  @Test
  void lastL3_globally_forbidden() {
    IndustryCategoryRepository mockCategoryRepo = mock(IndustryCategoryRepository.class);
    DataProductRepository mockProductRepo = mock(DataProductRepository.class);
    IndustryCategoryEntity only = makeCategoryEntity("l3-only", "独苗子类", "L3", "l2-x");
    when(mockCategoryRepo.findAll()).thenReturn(List.of(only));
    when(mockCategoryRepo.save(any())).thenReturn(only);
    when(mockProductRepo.findAll()).thenReturn(List.of());
    when(mockProductRepo.count()).thenReturn(0L);

    CatalogBrowseSeedStore isolated = new CatalogBrowseSeedStore(mockCategoryRepo, mockProductRepo);
    CategoryAdminService isolatedSvc = new CategoryAdminService(isolated, mock(AuthAuditLogger.class));
    ServiceResult deny = isolatedSvc.delete("l3-only", "admin", "corr-last");
    assertFalse(deny.ok());
    assertEquals(409, deny.httpStatus());
    assertEquals("ERR_CATEGORY_HAS_PRODUCTS", deny.code());
  }

  @Test
  void rejectInvalidL3Parent() {
    ServiceResult bad = service.create(body("坏子类", "L3", "cat-l1-health"));
    assertFalse(bad.ok());
    assertEquals(400, bad.httpStatus());
  }

  private String create(String name, String level, String parentId) {
    ServiceResult r = service.create(body(name, level, parentId));
    assertTrue(r.ok(), () -> "create failed: " + r.code() + " " + r.message());
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) r.data();
    return String.valueOf(data.get("id"));
  }

  private void seedCategory(String code, String name, String level, String parentCode) {
    IndustryCategoryEntity e = makeCategoryEntity(code, name, level, parentCode);
    categoryList.add(e);
  }

  private IndustryCategoryEntity makeCategoryEntity(String code, String name, String level, String parentCode) {
    IndustryCategoryEntity e = new IndustryCategoryEntity();
    e.setId((long) (categoryList.size() + 1));
    e.setCode(code);
    e.setName(name);
    e.setLevel(level);
    e.setParentCode(parentCode);
    return e;
  }

  private void seedProduct(String productCode, String l3code, String l1code, String l2code) {
    DataProductEntity e = DataProductEntity.builder()
        .productCode(productCode)
        .productName("seed-" + productCode)
        .productType("DATASET")
        .industryCategoryCode(l3code)
        .l1CategoryCode(l1code)
        .l2CategoryCode(l2code)
        .maintenanceStatus("MAINTAINED")
        .dataSource("SELF_PRODUCED")
        .deliveryMethod("FILE")
        .status("LISTED")
        .build();
    e.setId((long) (productList.size() + 1));
    productList.add(e);
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
