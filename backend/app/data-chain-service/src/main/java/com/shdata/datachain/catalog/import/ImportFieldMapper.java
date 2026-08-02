package com.shdata.datachain.catalog.productimport;

import com.shdata.datachain.catalog.browse.CatalogBrowseSeedStore;
import com.shdata.datachain.catalog.browse.CatalogCategory;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/** 将 v0729 模板中文列值映射为产品写入 body（含 typeSpecific）。 */
final class ImportFieldMapper {

  private final CatalogBrowseSeedStore catalog;
  private final AtomicInteger codeSeq = new AtomicInteger(1);

  ImportFieldMapper(CatalogBrowseSeedStore catalog) {
    this.catalog = catalog;
  }

  Map<String, Object> toWriteBody(Map<String, String> cells) throws RowException {
    String productName = cell(cells, "产品名称（必填）");
    String productTypeRaw = cell(cells, "产品类型（必填）");
    String categoryRaw = cell(cells, "行业分类（必填）");
    String summary = cell(cells, "产品简介（必填）");

    if (isBlank(productName)) {
      throw new RowException("ERR_IMPORT_ROW_INVALID", "必填字段缺失：产品名称（必填）");
    }
    if (isBlank(productTypeRaw)) {
      throw new RowException("ERR_IMPORT_ROW_INVALID", "必填字段缺失：产品类型（必填）");
    }
    if (isBlank(categoryRaw)) {
      throw new RowException("ERR_IMPORT_ROW_INVALID", "必填字段缺失：行业分类（必填）");
    }
    if (isBlank(summary)) {
      throw new RowException("ERR_IMPORT_ROW_INVALID", "必填字段缺失：产品简介（必填）");
    }

    String productType = mapProductType(productTypeRaw);
    if (productType == null) {
      throw new RowException("ERR_IMPORT_ROW_INVALID", "产品类型非法：" + productTypeRaw);
    }

    String l3Id = resolveL3(categoryRaw);
    if (l3Id == null) {
      throw new RowException(
          "ERR_CATEGORY_LEAF_REQUIRED", "行业分类无法解析到可挂载三级节点：" + categoryRaw);
    }

    rejectHeterogeneousColumns(productType, cells);

    String productCode = nextProductCode();

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("productName", productName.trim());
    body.put("productCode", productCode);
    body.put("productType", productType);
    body.put("l3CategoryId", l3Id);
    body.put("summary", summary.trim());

    String delivery = mapDelivery(cell(cells, "交付方式"));
    if (delivery != null) {
      body.put("deliveryMethod", delivery);
    }
    String updateFrequency = mapUpdateFrequency(cell(cells, "更新频率"));
    if (updateFrequency != null) {
      body.put("updateFrequency", updateFrequency);
    }

    Map<String, Object> typeSpecific = buildTypeSpecific(productType, cells);
    if (!typeSpecific.isEmpty()) {
      body.put("typeSpecific", typeSpecific);
    }
    return body;
  }

  private void rejectHeterogeneousColumns(String productType, Map<String, String> cells)
      throws RowException {
    switch (productType) {
      case "API" -> {
        requireBlankOrFail(cells, "数据规模", productType);
        requireBlankOrFail(cells, "数据形态", productType);
        requireBlankOrFail(cells, "数据内容描述", productType);
      }
      case "DATASET" -> {
        requireBlankOrFail(cells, "接口定义", productType);
        requireBlankOrFail(cells, "数据内容描述", productType);
      }
      case "REPORT", "OTHER" -> {
        requireBlankOrFail(cells, "接口定义", productType);
        requireBlankOrFail(cells, "字段描述", productType);
        requireBlankOrFail(cells, "数据样例", productType);
        requireBlankOrFail(cells, "数据规模", productType);
        requireBlankOrFail(cells, "数据形态", productType);
      }
      default -> {}
    }
  }

  private static void requireBlankOrFail(Map<String, String> cells, String col, String type)
      throws RowException {
    if (!isBlank(cell(cells, col))) {
      throw new RowException(
          "ERR_IMPORT_ROW_INVALID", "产品类型 " + type + " 不得填写异型列：" + col);
    }
  }

  private Map<String, Object> buildTypeSpecific(String productType, Map<String, String> cells)
      throws RowException {
    Map<String, Object> root = new LinkedHashMap<>();
    String timeRange = trimOrNull(cell(cells, "时间范围"));
    String regionScope = trimOrNull(cell(cells, "地域范围"));
    switch (productType) {
      case "API" -> {
        Map<String, Object> api = new LinkedHashMap<>();
        if (timeRange != null) {
          api.put("timeRange", timeRange);
        }
        if (regionScope != null) {
          api.put("regionScope", regionScope);
        }
        String swagger = cell(cells, "接口定义");
        if (!isBlank(swagger)) {
          api.put("swaggerFileContent", swagger);
          try {
            List<Map<String, Object>> endpoints = ImportOpenApiParser.parseEndpoints(swagger);
            api.put("endpoints", endpoints);
            if (!endpoints.isEmpty()) {
              Map<String, Object> first = endpoints.get(0);
              api.put(
                  "endpoint",
                  first.getOrDefault("method", "") + " " + first.getOrDefault("path", ""));
            }
          } catch (ImportOpenApiParser.ParseException ex) {
            throw new RowException("ERR_IMPORT_ROW_INVALID", ex.getMessage());
          }
        } else {
          api.put("endpoints", List.of());
        }
        String fieldDesc = trimOrNull(cell(cells, "字段描述"));
        if (fieldDesc != null) {
          api.put("fieldDescription", fieldDesc);
        }
        String sample = trimOrNull(cell(cells, "数据样例"));
        if (sample != null) {
          api.put("dataSample", sample);
        }
        root.put("api", api);
      }
      case "DATASET" -> {
        Map<String, Object> ds = new LinkedHashMap<>();
        if (timeRange != null) {
          ds.put("timeRange", timeRange);
        }
        if (regionScope != null) {
          ds.put("regionScope", regionScope);
        }
        String scale = trimOrNull(cell(cells, "数据规模"));
        if (scale != null) {
          ds.put("dataScale", scale);
        }
        String form = trimOrNull(cell(cells, "数据形态"));
        if (form != null) {
          ds.put("dataForm", form);
        }
        String fieldDesc = trimOrNull(cell(cells, "字段描述"));
        if (fieldDesc != null) {
          ds.put("fieldDescription", fieldDesc);
        }
        String sample = trimOrNull(cell(cells, "数据样例"));
        if (sample != null) {
          ds.put("dataSample", sample);
        }
        root.put("dataset", ds);
      }
      case "REPORT" -> {
        Map<String, Object> report = new LinkedHashMap<>();
        if (timeRange != null) {
          report.put("timeRange", timeRange);
        }
        if (regionScope != null) {
          report.put("regionScope", regionScope);
        }
        String content = trimOrNull(cell(cells, "数据内容描述"));
        if (content != null) {
          report.put("contentDescription", content);
        }
        root.put("report", report);
      }
      case "OTHER" -> {
        Map<String, Object> other = new LinkedHashMap<>();
        if (timeRange != null) {
          other.put("timeRange", timeRange);
        }
        if (regionScope != null) {
          other.put("regionScope", regionScope);
        }
        String content = trimOrNull(cell(cells, "数据内容描述"));
        if (content != null) {
          other.put("contentDescription", content);
        }
        root.put("other", other);
      }
      default -> {}
    }
    return root;
  }

  private String nextProductCode() {
    for (int i = 0; i < 10_000; i++) {
      String code = String.format(Locale.ROOT, "WSC-IMP-%04d", codeSeq.getAndIncrement());
      if (catalog.findProductByCode(code).isEmpty()) {
        return code;
      }
    }
    throw new IllegalStateException("unable to allocate product code");
  }

  private String resolveL3(String raw) {
    String v = raw.trim();
    Optional<CatalogCategory> byId = catalog.findCategory(v);
    if (byId.isPresent()) {
      CatalogCategory c = byId.get();
      if ("L3".equals(c.level())) {
        return c.id();
      }
      return null;
    }
    String normalized = v.replace('／', '/').replaceAll("\\s*/\\s*", " / ").trim();
    for (CatalogCategory c : catalog.categories()) {
      if (!"L3".equals(c.level())) {
        continue;
      }
      if (c.name() != null && c.name().equalsIgnoreCase(v)) {
        return c.id();
      }
      String path = catalog.resolveCategoryPathFromL3(c.id());
      if (path != null && path.equalsIgnoreCase(normalized)) {
        return c.id();
      }
      String compact = path == null ? "" : path.replace(" / ", "/");
      String compactIn = normalized.replace(" / ", "/");
      if (compact.equalsIgnoreCase(compactIn)) {
        return c.id();
      }
    }
    // L2 名称：若其下恰有一个 L3 则采纳，否则无法唯一挂载
    for (CatalogCategory c : catalog.categories()) {
      if (!"L2".equals(c.level())) {
        continue;
      }
      if (c.name() == null || !c.name().equalsIgnoreCase(v)) {
        continue;
      }
      List<String> children = new java.util.ArrayList<>();
      for (CatalogCategory child : catalog.categories()) {
        if ("L3".equals(child.level()) && c.id().equals(child.parentId())) {
          children.add(child.id());
        }
      }
      if (children.size() == 1) {
        return children.get(0);
      }
    }
    return null;
  }

  private static String mapProductType(String raw) {
    String v = raw.trim();
    return switch (v.toUpperCase(Locale.ROOT)) {
      case "DATASET", "数据集" -> "DATASET";
      case "REPORT", "数据报告" -> "REPORT";
      case "API", "数据接口" -> "API";
      case "OTHER", "其他数据产品", "其他" -> "OTHER";
      default -> {
        Map<String, String> cn = new HashMap<>();
        cn.put("数据集", "DATASET");
        cn.put("数据报告", "REPORT");
        cn.put("数据接口", "API");
        cn.put("其他数据产品", "OTHER");
        yield cn.get(v);
      }
    };
  }

  private static String mapDelivery(String raw) {
    if (isBlank(raw)) {
      return null;
    }
    String v = raw.trim();
    return switch (v.toUpperCase(Locale.ROOT)) {
      case "API" -> "API";
      case "FILE", "文件传输" -> "FILE";
      case "SANDBOX", "数据沙箱" -> "SANDBOX";
      case "PRIVACY_COMPUTE", "隐私保护计算" -> "PRIVACY_COMPUTE";
      default -> {
        Map<String, String> cn =
            Map.of(
                "文件传输", "FILE",
                "数据沙箱", "SANDBOX",
                "隐私保护计算", "PRIVACY_COMPUTE",
                "API", "API");
        yield cn.getOrDefault(v, v);
      }
    };
  }

  private static String mapUpdateFrequency(String raw) {
    if (isBlank(raw)) {
      return null;
    }
    String v = raw.trim();
    Map<String, String> cn = new HashMap<>();
    cn.put("实时", "REALTIME");
    cn.put("每日", "DAILY");
    cn.put("每周", "WEEKLY");
    cn.put("每月", "MONTHLY");
    cn.put("每年", "YEARLY");
    cn.put("按需", "ON_DEMAND");
    cn.put("不更新", "NO_UPDATE");
    cn.put("REALTIME", "REALTIME");
    cn.put("DAILY", "DAILY");
    cn.put("DAY", "DAILY");
    cn.put("WEEKLY", "WEEKLY");
    cn.put("WEEK", "WEEKLY");
    cn.put("MONTHLY", "MONTHLY");
    cn.put("MONTH", "MONTHLY");
    cn.put("YEARLY", "YEARLY");
    cn.put("YEAR", "YEARLY");
    cn.put("ON_DEMAND", "ON_DEMAND");
    cn.put("NO_UPDATE", "NO_UPDATE");
    return cn.getOrDefault(v, v);
  }

  private static String cell(Map<String, String> cells, String key) {
    String v = cells.get(key);
    return v == null ? "" : v;
  }

  private static String trimOrNull(String s) {
    if (isBlank(s)) {
      return null;
    }
    return s.trim();
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  static final class RowException extends Exception {
    final String code;
    final String message;

    RowException(String code, String message) {
      super(message);
      this.code = code;
      this.message = message;
    }
  }
}
