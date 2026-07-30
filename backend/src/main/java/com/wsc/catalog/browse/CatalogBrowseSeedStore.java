package com.wsc.catalog.browse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * 内存目录数据（浏览 + 写路径共用）。
 *
 * <p>SCOPE_AMEND（TASK-WSC-005）：增加可变 API，供 editor/admin 写路径与 browse 只读共享同一状态。
 */
@Component
public class CatalogBrowseSeedStore {

  private final List<CatalogCategory> categories = new CopyOnWriteArrayList<>();
  private final List<CatalogProduct> products = new CopyOnWriteArrayList<>();
  private final AtomicInteger productSeq = new AtomicInteger(100);
  private final AtomicInteger categorySeq = new AtomicInteger(100);

  public CatalogBrowseSeedStore() {
    seed();
  }

  public List<CatalogCategory> categories() {
    return List.copyOf(categories);
  }

  public List<CatalogProduct> products() {
    return List.copyOf(products);
  }

  public Optional<CatalogProduct> findProduct(String id) {
    return products.stream().filter(p -> p.id().equals(id)).findFirst();
  }

  public Optional<CatalogProduct> findProductByCode(String productCode) {
    if (productCode == null) {
      return Optional.empty();
    }
    return products.stream().filter(p -> productCode.equals(p.productCode())).findFirst();
  }

  public Optional<CatalogCategory> findCategory(String id) {
    return categories.stream().filter(c -> c.id().equals(id)).findFirst();
  }

  public synchronized String nextProductId() {
    return "prod-gen-" + productSeq.incrementAndGet();
  }

  public synchronized String nextCategoryId() {
    return "cat-gen-" + categorySeq.incrementAndGet();
  }

  public synchronized void upsertProduct(CatalogProduct product) {
    products.removeIf(p -> p.id().equals(product.id()));
    products.add(product);
  }

  public synchronized void addCategory(CatalogCategory category) {
    categories.add(category);
  }

  public synchronized void replaceCategory(CatalogCategory category) {
    categories.removeIf(c -> c.id().equals(category.id()));
    categories.add(category);
  }

  public synchronized boolean removeCategory(String categoryId) {
    return categories.removeIf(c -> c.id().equals(categoryId));
  }

  /** 挂载计数：L2 按 l2CategoryId；L1 按 l1CategoryId（含其下所有产品）。 */
  public int countMountedProducts(CatalogCategory category) {
    if ("L1".equals(category.level())) {
      return (int) products.stream().filter(p -> category.id().equals(p.l1CategoryId())).count();
    }
    return (int) products.stream().filter(p -> category.id().equals(p.l2CategoryId())).count();
  }

  public String resolveCategoryPath(String l2CategoryId) {
    Optional<CatalogCategory> l2 = findCategory(l2CategoryId);
    if (l2.isEmpty()) {
      return "";
    }
    String l1Name =
        l2.get().parentId() == null
            ? ""
            : findCategory(l2.get().parentId()).map(CatalogCategory::name).orElse("");
    if (l1Name.isEmpty()) {
      return l2.get().name();
    }
    return l1Name + " / " + l2.get().name();
  }

  public String resolveL1Id(String l2CategoryId) {
    return findCategory(l2CategoryId).map(CatalogCategory::parentId).orElse(null);
  }

  private void seed() {
    categories.add(new CatalogCategory("cat-l1-health", "医疗卫生", "L1", null));
    categories.add(new CatalogCategory("cat-l1-finance", "金融服务", "L1", null));

    categories.add(new CatalogCategory("cat-l2-emr", "电子病历", "L2", "cat-l1-health"));
    categories.add(new CatalogCategory("cat-l2-imaging", "医学影像", "L2", "cat-l1-health"));
    categories.add(new CatalogCategory("cat-l2-credit", "征信评估", "L2", "cat-l1-finance"));
    categories.add(new CatalogCategory("cat-l2-txn", "交易流水", "L2", "cat-l1-finance"));

    products.add(
        product(
            "prod-emr-001",
            "MED-EMR-0001",
            "电子病历脱敏数据集",
            "DATASET",
            "cat-l2-emr",
            "cat-l1-health",
            "医疗卫生 / 电子病历",
            3,
            3,
            "SELF_PRODUCED",
            "FILE",
            true,
            true,
            "覆盖多院区结构化病历字段，供科研脱敏分析。",
            "临床科研、疾病谱分析",
            "华康数据科技",
            "91310000MA1KXXXX1A",
            List.of("病历", "脱敏"),
            Map.of("dataset", Map.of("recordCount", 120000)),
            "卫生",
            "病历",
            "DAY",
            "按次",
            "面议",
            "数据使用权"));

    products.add(
        product(
            "prod-img-001",
            "MED-IMG-0002",
            "胸部 CT 影像报告",
            "REPORT",
            "cat-l2-imaging",
            "cat-l1-health",
            "医疗卫生 / 医学影像",
            1,
            1,
            "AGREEMENT",
            "API",
            false,
            true,
            "标准化胸部 CT 影像诊断报告摘要。",
            "辅助诊断质控",
            "影像云服务",
            "91310000MA1KXXXX2B",
            List.of("影像", "报告"),
            Map.of("report", Map.of("pageCount", 12)),
            "卫生",
            "影像",
            "WEEK",
            "包年",
            "面议",
            "数据使用权"));

    products.add(
        product(
            "prod-credit-001",
            "FIN-CRD-0003",
            "企业征信评分接口",
            "API",
            "cat-l2-credit",
            "cat-l1-finance",
            "金融服务 / 征信评估",
            5,
            5,
            "DERIVED",
            "API",
            false,
            false,
            "按统一社会信用代码返回企业征信评分。",
            "贷前风控",
            "信达征信",
            "91310000MA1KXXXX3C",
            List.of("征信", "评分"),
            Map.of("api", Map.of("endpoint", "/v1/credit/score")),
            "金融",
            "征信",
            "REALTIME",
            "按调用",
            "面议",
            "数据使用权"));

    products.add(
        product(
            "prod-txn-001",
            "FIN-TXN-0004",
            "零售支付流水样本",
            "DATASET",
            "cat-l2-txn",
            "cat-l1-finance",
            "金融服务 / 交易流水",
            2,
            2,
            "PUBLIC_COLLECT",
            "SANDBOX",
            true,
            false,
            "脱敏零售支付流水样本，支持沙箱联调。",
            "支付行为分析",
            "汇通支付",
            "91310000MA1KXXXX4D",
            List.of("支付", "流水"),
            Map.of("dataset", Map.of("recordCount", 50000)),
            "金融",
            "支付",
            "DAY",
            "免费试用",
            "0",
            "数据使用权"));

    products.add(
        product(
            "prod-misc-001",
            "GEN-MISC-0005",
            "行业通用其他数据产品",
            "OTHER",
            "cat-l2-emr",
            "cat-l1-health",
            "医疗卫生 / 电子病历",
            0,
            null,
            "SELF_PRODUCED",
            "PRIVACY_COMPUTE",
            false,
            false,
            "其他类型示例（无类型专属字段，OQ-004）。",
            "通用对接演示",
            "示例供应商",
            "91310000MA1KXXXX5E",
            List.of("其他"),
            Map.of(),
            "通用",
            "其他",
            "MONTH",
            "面议",
            "面议",
            "数据使用权"));
  }

  private static CatalogProduct product(
      String id,
      String code,
      String name,
      String type,
      String l2,
      String l1,
      String path,
      int chainCount,
      Integer latestVersionNo,
      String dataSource,
      String delivery,
      boolean publicData,
      boolean pii,
      String summary,
      String scenario,
      String supplier,
      String creditCode,
      List<String> tags,
      Map<String, Object> typeSpecific,
      String businessCategory,
      String businessSubCategory,
      String updateFrequency,
      String billingMethod,
      String price,
      String propertyRightsType) {
    return new CatalogProduct(
        id,
        code,
        name,
        type,
        l2,
        l1,
        path,
        chainCount,
        latestVersionNo,
        dataSource,
        delivery,
        publicData,
        pii,
        summary,
        scenario,
        supplier,
        creditCode,
        new ArrayList<>(tags),
        new LinkedHashMap<>(typeSpecific),
        businessCategory,
        businessSubCategory,
        updateFrequency,
        billingMethod,
        price,
        propertyRightsType);
  }
}
