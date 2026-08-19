package com.shdata.datachain.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.entity.DataProductEntity;
import com.shdata.datachain.entity.IndustryCategoryEntity;
import com.shdata.datachain.model.CatalogCategory;
import com.shdata.datachain.model.CatalogProduct;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CatalogProduct / CatalogCategory ↔ JPA Entity 映射工具。
 * <p>跨包映射工具，供 repository 层 Service 层使用。</p>
 */
public final class CatalogEntityMapper {

    private static final ObjectMapper JSON = new ObjectMapper();

    private CatalogEntityMapper() {
    }

    /**
     * DataProductEntity → CatalogProduct
     */
    public static CatalogProduct toProduct(DataProductEntity e) {
        return toProduct(e, 0, null);
    }

    /**
     * DataProductEntity → CatalogProduct，并带入从上链版本表汇总的链元数据。
     *
     * @param e 产品实体
     * @param chainCount 已落库链版本数量
     * @param latestVersionNo 最新链版本号；无版本时为 null
     * @return 产品领域模型
     */
    public static CatalogProduct toProduct(
            DataProductEntity e, int chainCount, Integer latestVersionNo) {
        return new CatalogProduct(
                e.getId() == null ? "" : String.valueOf(e.getId()),
                e.getProductCode(),
                e.getProductName(),
                e.getProductType(),
                e.getL2CategoryCode(),
                e.getL1CategoryCode(),
                e.getCategoryPath(),
                chainCount,
                latestVersionNo,
                e.getDataSource(),
                e.getDeliveryMethod(),
                e.getInvolvesPublicData(),
                e.getInvolvesPersonalInfo(),
                e.getSummary(),
                e.getScenario(),
                e.getSupplierName(),
                e.getSupplierCreditCode(),
                parseTags(e.getTagsJson()),
                parseTypeSpecific(e),
                e.getBusinessCategory(),
                e.getBusinessSubCategory(),
                e.getUpdateFrequency(),
                e.getBillingMethod(),
                e.getPrice(),
                e.getPropertyRightsType(),
                e.getIndustryCategoryCode(),
                e.getBusinessCategory(),
                toInstant(e.getUpdateTime()));
    }

    /**
     * CatalogProduct → DataProductEntity。id 始终不设，由调用方（upsertProduct）按 product_code 匹配后决定。
     */
    public static DataProductEntity toEntity(CatalogProduct p) {
        DataProductEntity e = new DataProductEntity();
        e.setProductCode(p.productCode());
        e.setProductName(p.productName());
        e.setProductType(p.productType());
        e.setIndustryCategoryCode(p.l3CategoryId());
        e.setL1CategoryCode(p.l1CategoryId());
        e.setL2CategoryCode(p.l2CategoryId());
        e.setCategoryPath(p.categoryPath());
        e.setMaintenanceStatus(p.l3CategoryId() != null && !p.l3CategoryId().isBlank() ? "MAINTAINED" : "PENDING");
        e.setBusinessCategory(p.businessCategory());
        e.setBusinessSubCategory(p.businessSubCategory());
        e.setDataSource(p.dataSource());
        e.setUpdateFrequency(p.updateFrequency());
        e.setDeliveryMethod(p.deliveryMethod());
        e.setInvolvesPersonalInfo(p.involvesPersonalInfo());
        e.setInvolvesPublicData(p.involvesPublicData());
        e.setBillingMethod(p.billingMethod());
        e.setPrice(p.price());
        e.setSupplierName(p.supplierName());
        e.setSupplierCreditCode(p.supplierCreditCode());
        e.setPropertyRightsType(p.propertyRightsType());
        e.setTagsJson(toJson(p.tags()));
        e.setSummary(p.summary());
        e.setScenario(p.scenario());
        e.setStatus("LISTED");

        // typeSpecific 按类型拆分
        if (p.typeSpecific() != null) {
            Map<String, Object> ts = p.typeSpecific();
            if (ts.containsKey("dataset")) {
                e.setTypeDatasetJson(toJson(ts.get("dataset")));
            }
            if (ts.containsKey("report")) {
                e.setTypeReportJson(toJson(ts.get("report")));
            }
            if (ts.containsKey("api")) {
                e.setTypeApiJson(toJson(ts.get("api")));
            }
            if (ts.containsKey("other")) {
                e.setTypeApiJson(toJson(ts.get("other")));
            }
        }
        return e;
    }

    /**
     * IndustryCategoryEntity → CatalogCategory
     */
    public static CatalogCategory toCategory(IndustryCategoryEntity e) {
        return new CatalogCategory(e.getCode(), e.getName(), e.getLevel(), e.getParentCode());
    }

    /**
     * CatalogCategory → IndustryCategoryEntity
     */
    public static IndustryCategoryEntity toEntity(CatalogCategory c) {
        IndustryCategoryEntity e = new IndustryCategoryEntity();
        e.setCode(c.id());
        e.setName(c.name());
        e.setLevel(c.level());
        e.setParentCode(c.parentId());
        return e;
    }

    private static List<String> parseTags(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return JSON.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseTypeSpecific(DataProductEntity e) {
        Map<String, Object> root = new LinkedHashMap<>();
        if (e.getTypeDatasetJson() != null) {
            Map<String, Object> ds = parseJsonMap(e.getTypeDatasetJson());
            if (!ds.isEmpty()) root.put("dataset", ds);
        }
        if (e.getTypeReportJson() != null) {
            Map<String, Object> r = parseJsonMap(e.getTypeReportJson());
            if (!r.isEmpty()) root.put("report", r);
        }
        if (e.getTypeApiJson() != null) {
            Map<String, Object> api = parseJsonMap(e.getTypeApiJson());
            if (!api.isEmpty()) root.put("api", api);
        }
        return root;
    }

    private static Map<String, Object> parseJsonMap(String json) {
        try {
            return JSON.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private static String toJson(Object obj) {
        try {
            return JSON.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private static Instant toInstant(LocalDateTime dt) {
        return dt == null ? null : dt.atZone(ZoneId.of("UTC")).toInstant();
    }
}
