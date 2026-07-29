package com.wsc.catalog.browse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/** 内存种子目录数据（至少 2 个 L1、各有 L2、多种 productType）。 */
@Component
public class CatalogBrowseSeedStore {

  private final List<CatalogCategory> categories = new CopyOnWriteArrayList<>();
  private final List<CatalogProduct> products = new CopyOnWriteArrayList<>();

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
            Map.of("dataset", Map.of("recordCount", 120000))));

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
            Map.of("report", Map.of("pageCount", 12))));

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
            Map.of("api", Map.of("endpoint", "/v1/credit/score"))));

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
            Map.of("dataset", Map.of("recordCount", 50000))));

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
            Map.of()));
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
      Map<String, Object> typeSpecific) {
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
        new LinkedHashMap<>(typeSpecific));
  }
}
