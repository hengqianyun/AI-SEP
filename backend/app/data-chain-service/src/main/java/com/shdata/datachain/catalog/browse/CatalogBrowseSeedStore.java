package com.shdata.datachain.catalog.browse;

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
 * <p>V1.1：种子含 L1/L2/L3；产品默认挂三级。挂载计数对 L3 按 {@code l3CategoryId}，对 L2/L1 向上汇总。
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

  /**
   * 挂载计数：L3 按 l3CategoryId；L2 含直挂 l2 与其下 L3 产品；L1 按 l1CategoryId。
   */
  public int countMountedProducts(CatalogCategory category) {
    if ("L1".equals(category.level())) {
      return (int) products.stream().filter(p -> category.id().equals(p.l1CategoryId())).count();
    }
    if ("L3".equals(category.level())) {
      return (int)
          products.stream().filter(p -> category.id().equals(p.l3CategoryId())).count();
    }
    // L2：直挂 + 子 L3
    return (int)
        products.stream()
            .filter(
                p ->
                    category.id().equals(p.l2CategoryId())
                        || (p.l3CategoryId() != null
                            && findCategory(p.l3CategoryId())
                                .map(c -> category.id().equals(c.parentId()))
                                .orElse(false)))
            .count();
  }

  /** 二级路径「L1 / L2」（editor 兼容）。 */
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

  /** 三级完整路径「空间 / 行业 / 子类」。 */
  public String resolveCategoryPathFromL3(String l3CategoryId) {
    Optional<CatalogCategory> l3 = findCategory(l3CategoryId);
    if (l3.isEmpty() || !"L3".equals(l3.get().level())) {
      return "";
    }
    Optional<CatalogCategory> l2 =
        l3.get().parentId() == null ? Optional.empty() : findCategory(l3.get().parentId());
    String l2Name = l2.map(CatalogCategory::name).orElse("");
    String l1Name =
        l2.map(CatalogCategory::parentId)
            .flatMap(this::findCategory)
            .map(CatalogCategory::name)
            .orElse("");
    if (l1Name.isEmpty() && l2Name.isEmpty()) {
      return l3.get().name();
    }
    if (l1Name.isEmpty()) {
      return l2Name + " / " + l3.get().name();
    }
    return l1Name + " / " + l2Name + " / " + l3.get().name();
  }

  public List<String> resolvePathLabels(String categoryId) {
    Optional<CatalogCategory> node = findCategory(categoryId);
    if (node.isEmpty()) {
      return List.of();
    }
    List<String> labels = new ArrayList<>();
    CatalogCategory cur = node.get();
    labels.add(0, cur.name());
    while (cur.parentId() != null) {
      Optional<CatalogCategory> parent = findCategory(cur.parentId());
      if (parent.isEmpty()) {
        break;
      }
      cur = parent.get();
      labels.add(0, cur.name());
    }
    return labels;
  }

  public String resolveL1Id(String l2CategoryId) {
    return findCategory(l2CategoryId).map(CatalogCategory::parentId).orElse(null);
  }

  public String resolveL2IdFromL3(String l3CategoryId) {
    return findCategory(l3CategoryId)
        .filter(c -> "L3".equals(c.level()))
        .map(CatalogCategory::parentId)
        .orElse(null);
  }

  public String resolveL1IdFromL3(String l3CategoryId) {
    String l2 = resolveL2IdFromL3(l3CategoryId);
    return l2 == null ? null : resolveL1Id(l2);
  }

  private void seed() {
    // L1 空间
    categories.add(new CatalogCategory("cat-l1-health", "医疗卫生", "L1", null));
    categories.add(new CatalogCategory("cat-l1-finance", "金融服务", "L1", null));

    // L2 行业
    categories.add(new CatalogCategory("cat-l2-emr", "电子病历", "L2", "cat-l1-health"));
    categories.add(new CatalogCategory("cat-l2-imaging", "医学影像", "L2", "cat-l1-health"));
    categories.add(new CatalogCategory("cat-l2-credit", "征信评估", "L2", "cat-l1-finance"));
    categories.add(new CatalogCategory("cat-l2-txn", "交易流水", "L2", "cat-l1-finance"));

    // L3 子类
    categories.add(new CatalogCategory("cat-l3-emr-desense", "脱敏病历", "L3", "cat-l2-emr"));
    categories.add(new CatalogCategory("cat-l3-emr-struct", "结构化病历", "L3", "cat-l2-emr"));
    categories.add(new CatalogCategory("cat-l3-img-ct", "CT 影像", "L3", "cat-l2-imaging"));
    categories.add(new CatalogCategory("cat-l3-credit-score", "征信评分", "L3", "cat-l2-credit"));
    categories.add(new CatalogCategory("cat-l3-txn-retail", "零售支付", "L3", "cat-l2-txn"));

    products.add(
        product(
            "prod-emr-001",
            "MED-EMR-0001",
            "电子病历脱敏数据集",
            "DATASET",
            "cat-l3-emr-desense",
            "cat-l2-emr",
            "cat-l1-health",
            "医疗卫生 / 电子病历 / 脱敏病历",
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
            "prod-emr-002",
            "MED-EMR-0006",
            "结构化门诊病历样本",
            "DATASET",
            "cat-l3-emr-struct",
            "cat-l2-emr",
            "cat-l1-health",
            "医疗卫生 / 电子病历 / 结构化病历",
            1,
            1,
            "SELF_PRODUCED",
            "FILE",
            false,
            true,
            "门诊结构化病历字段样本。",
            "门诊质控",
            "华康数据科技",
            "91310000MA1KXXXX1A",
            List.of("门诊"),
            Map.of("dataset", Map.of("recordCount", 8000)),
            "卫生",
            "病历",
            "WEEK",
            "按次",
            "面议",
            "数据使用权"));

    products.add(
        product(
            "prod-img-001",
            "MED-IMG-0002",
            "胸部 CT 影像报告",
            "REPORT",
            "cat-l3-img-ct",
            "cat-l2-imaging",
            "cat-l1-health",
            "医疗卫生 / 医学影像 / CT 影像",
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
            "cat-l3-credit-score",
            "cat-l2-credit",
            "cat-l1-finance",
            "金融服务 / 征信评估 / 征信评分",
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
            "cat-l3-txn-retail",
            "cat-l2-txn",
            "cat-l1-finance",
            "金融服务 / 交易流水 / 零售支付",
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
            "cat-l3-emr-desense",
            "cat-l2-emr",
            "cat-l1-health",
            "医疗卫生 / 电子病历 / 脱敏病历",
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

    // 额外种子便于滚动分页（同筛选下多页）
    for (int i = 1; i <= 18; i++) {
      String n = String.format("%02d", i);
      products.add(
          product(
              "prod-page-" + n,
              "MED-PG-" + String.format("%04d", i),
              "脱敏病历分页样例 " + n,
              "DATASET",
              "cat-l3-emr-desense",
              "cat-l2-emr",
              "cat-l1-health",
              "医疗卫生 / 电子病历 / 脱敏病历",
              0,
              null,
              "SELF_PRODUCED",
              "FILE",
              true,
              false,
              "分页加载演示产品 " + n,
              "目录滚动加载",
              "华康数据科技",
              "91310000MA1KXXXX1A",
              List.of("分页"),
              Map.of("dataset", Map.of("recordCount", i * 100)),
              "卫生",
              "病历",
              "DAY",
              "按次",
              "面议",
              "数据使用权"));
    }
  }

  private static CatalogProduct product(
      String id,
      String code,
      String name,
      String type,
      String l3,
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
        propertyRightsType,
        l3);
  }
}
