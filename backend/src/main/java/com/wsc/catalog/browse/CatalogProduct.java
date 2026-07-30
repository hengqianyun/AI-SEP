package com.wsc.catalog.browse;

import java.util.List;
import java.util.Map;

/** 目录产品（列表/预览/详情/编辑共用字段）。SCOPE_AMEND：TASK-WSC-005 扩展详情分组字段。 */
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
    Map<String, Object> typeSpecific,
    String businessCategory,
    String businessSubCategory,
    String updateFrequency,
    String billingMethod,
    String price,
    String propertyRightsType) {}
