package com.wsc.catalog.browse;

import java.util.List;
import java.util.Map;

/** 目录产品（预览/列表最小集 + 筛选字段）。 */
public record CatalogProduct(
    String id,
    String productCode,
    String productName,
    String productType,
    String l2CategoryId,
    String l1CategoryId,
    String categoryPath,
    int chainCount,
    Integer latestVersionNo,
    String dataSource,
    String deliveryMethod,
    Boolean involvesPublicData,
    Boolean involvesPersonalInfo,
    String summary,
    String scenario,
    String supplierName,
    String supplierCreditCode,
    List<String> tags,
    Map<String, Object> typeSpecific) {}
