package com.shdata.datachain.service.catalog.editor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.common.constant.IndustryCategories;
import com.shdata.datachain.common.exception.BusinessException;
import com.shdata.datachain.common.port.ChainAttestationPort;
import com.shdata.datachain.common.response.ServiceResult;
import com.shdata.datachain.common.security.AuthAuditLogger;
import com.shdata.datachain.model.CatalogCategory;
import com.shdata.datachain.model.CatalogProduct;
import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import com.shdata.datachain.repository.InMemoryChainStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 产品新增/编辑 + 同事务模拟存证（DEC-WSC-001/002；TASK-WSC-301 typeSpecific；DEC-WSC-006）。
 *
 * <p>适配失败时不落产品半成品、不写孤儿上链行。必填 {@code industryCategory}（GB/T 门类）；
 * {@code l3CategoryId} 可空；若显式传入则须为可挂载 L3。
 * OTHER 允许 {@code typeSpecific.other.contentDescription}；API 写冲突以客户端 endpoints 为准。
 */
@Service
public class ProductEditorService {

    private static final Logger log = LoggerFactory.getLogger(ProductEditorService.class);

    public static final Pattern PRODUCT_CODE = Pattern.compile("^[A-Z0-9]+-[A-Z0-9]+-[0-9]{4,}$");

    private final CatalogBrowseSeedStore catalog;
    private final ChainAttestationPort attestationPort;
    private final InMemoryChainStore chainStore;
    private final ObjectMapper objectMapper;
    private final AuthAuditLogger auditLogger;

    public ProductEditorService(
            CatalogBrowseSeedStore catalog,
            ChainAttestationPort attestationPort,
            InMemoryChainStore chainStore,
            ObjectMapper objectMapper,
            AuthAuditLogger auditLogger) {
        this.catalog = catalog;
        this.attestationPort = attestationPort;
        this.chainStore = chainStore;
        this.objectMapper = objectMapper;
        this.auditLogger = auditLogger;
    }

    /**
     * 新增数据产品，流程：校验字段 → 存证上链 → 写入目录内存。
     * <p>必填：productName、productCode（格式 {DOMAIN}-{FEATURE}-{NNNN}）、productType、industryCategory（GB/T 门类）。
     * l3CategoryId 可选（传入则须为 L3 叶子节点）。</p>
     * <p>存证失败时不写入产品，返回 500。</p>
     *
     * @param body          产品字段 Map
     * @param actorUserId   操作人用户 ID
     * @param correlationId 请求追踪 ID
     * @return 创建后的产品（含版本号）
     */
    public ServiceResult create(Map<String, Object> body, String actorUserId, String correlationId) {
        String codeHint = stringVal(body == null ? null : body.get("productCode"), "-");
        try {
            ValidatedWrite write = validateWrite(body, null);
            String productId = catalog.nextProductId();
            CatalogProduct draft = toProduct(productId, write, 0, null);
            Attested attested = attestAndStore(draft, 1);
            CatalogProduct published = withChainMeta(draft, 1, attested.versionNo());
            CatalogProduct saved = catalog.upsertProduct(published, actorUserId);
            auditLogger.productSubmit(actorUserId, saved.productCode(), "SUCCESS", null, correlationId);
            return ServiceResult.ok(toMap(saved));
        } catch (BusinessException ex) {
            auditLogger.productSubmit(actorUserId, codeHint, "FAILURE", ex.code(), correlationId);
            return ServiceResult.fail(ex.httpStatus(), ex.code(), ex.getMessage(), ex.data());
        } catch (RuntimeException ex) {
            log.error("Product create failed: codeHint={}", codeHint, ex);
            auditLogger.productSubmit(actorUserId, codeHint, "FAILURE", "ERR_ATTESTATION", correlationId);
            return ServiceResult.fail(500, "ERR_ATTESTATION", "存证适配失败: " + ex.getMessage(), null);
        }
    }

    /**
     * 编辑已有数据产品，版本号递增，重新存证上链。
     * <p>校验规则同 create；版本号取当前产品最大版本 +1。</p>
     *
     * @param productId     产品 ID
     * @param body          产品字段 Map
     * @param actorUserId   操作人用户 ID
     * @param correlationId 请求追踪 ID
     * @return 更新后的产品
     */
    public ServiceResult update(
            String productId, Map<String, Object> body, String actorUserId, String correlationId) {
        Optional<CatalogProduct> existingOpt = catalog.findProduct(productId);
        if (existingOpt.isEmpty()) {
            return ServiceResult.fail(404, "404", "产品不存在", null);
        }
        // SNAP-WSC-005 / REQ-CAT-010：空/缺失/异主 create_by 一律 403，禁止 PROVIDER 冒领
        if (!catalog.isOwnedBy(productId, actorUserId)) {
            return ServiceResult.fail(403, "ERR_FORBIDDEN", "只能编辑本人创建的产品", null);
        }
        CatalogProduct existing = existingOpt.get();
        try {
            ValidatedWrite write = validateWrite(body, productId);
            int priorChain =
                    Math.max(
                            nullSafe(existing.chainCount()),
                            chainStore.listByProductId(existing.productCode()).size());
            int nextVersion =
                    Math.max(
                            chainStore.latestVersionNo(existing.productCode()),
                            nullSafe(existing.latestVersionNo()))
                            + 1;
            CatalogProduct draft =
                    toProduct(productId, write, priorChain, existing.latestVersionNo());
            Attested attested = attestAndStore(draft, nextVersion);
            CatalogProduct published =
                    withChainMeta(draft, priorChain + 1, attested.versionNo());
            CatalogProduct saved = catalog.upsertProduct(published, actorUserId);
            auditLogger.productSubmit(actorUserId, saved.productCode(), "SUCCESS", null, correlationId);
            return ServiceResult.ok(toMap(saved));
        } catch (BusinessException ex) {
            auditLogger.productSubmit(actorUserId, existing.productCode(), "FAILURE", ex.code(), correlationId);
            return ServiceResult.fail(ex.httpStatus(), ex.code(), ex.getMessage(), ex.data());
        } catch (RuntimeException ex) {
            log.error("Product update failed: productId={}", productId, ex);
            auditLogger.productSubmit(
                    actorUserId, existing.productCode(), "FAILURE", "ERR_ATTESTATION", correlationId);
            return ServiceResult.fail(500, "ERR_ATTESTATION", "存证适配失败: " + ex.getMessage(), null);
        }
    }

    /**
     * 删除已有数据产品（逻辑删除）。
     * <p>404 若不存在；403 若非本人 create_by（与 update 同权）。</p>
     *
     * @param productId     产品 ID
     * @param actorUserId   操作人用户 ID
     * @param correlationId 请求追踪 ID
     * @return 删除结果（{@code deleted: true}）
     */
    public ServiceResult delete(String productId, String actorUserId, String correlationId) {
        Optional<CatalogProduct> existingOpt = catalog.findProduct(productId);
        if (existingOpt.isEmpty()) {
            return ServiceResult.fail(404, "404", "产品不存在", null);
        }
        if (!catalog.isOwnedBy(productId, actorUserId)) {
            return ServiceResult.fail(403, "ERR_FORBIDDEN", "只能删除本人创建的产品", null);
        }
        CatalogProduct existing = existingOpt.get();
        boolean removed = catalog.removeProduct(productId);
        if (!removed) {
            return ServiceResult.fail(404, "404", "产品不存在", null);
        }
        auditLogger.productSubmit(actorUserId, existing.productCode(), "SUCCESS", null, correlationId);
        return ServiceResult.ok(Map.of("deleted", true));
    }

    private static int nullSafe(Integer v) {
        return v == null ? 0 : v;
    }

    private Attested attestAndStore(CatalogProduct product, int versionNo) {
        String versionId =
                "cv-" + product.id() + "-v" + versionNo + "-" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> snapshot = buildSnapshot(product, versionId, versionNo);
        String snapshotJson = writeJson(snapshot);

        ChainAttestationPort.AttestationResult attested =
                attestationPort.attest(
                        new ChainAttestationPort.AttestationRequest(
                                product.productCode(), versionNo, snapshotJson));

        Map<String, Object> attestation = new LinkedHashMap<>();
        attestation.put("metadataHash", attested.metadataHash());
        attestation.put("ownerDID", attested.ownerDID());
        attestation.put("timestamp", attested.timestamp().toString());
        attestation.put("certificate", Map.of("owner", attested.certificate().owner()));
        snapshot.put("attestation", attestation);
        snapshot.put("capturedAt", attested.timestamp().toString());
        snapshotJson = writeJson(snapshot);

        chainStore.appendVersion(
                versionId,
                product.productCode(),
                versionNo,
                attested.metadataHash(),
                attested.ownerDID(),
                attested.timestamp(),
                attested.certificate().owner(),
                snapshotJson);
        return new Attested(versionNo, versionId);
    }

    private String writeJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("snapshot serialize failed", e);
        }
    }

    private Map<String, Object> buildSnapshot(CatalogProduct product, String versionId, int versionNo) {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("versionId", versionId);
        snap.put("productCode", product.productCode());
        snap.put("productName", product.productName());
        snap.put("productType", product.productType());
        snap.put("categoryPath", product.categoryPath());
        snap.put("categoryPathParts", categoryPathParts(product.l3CategoryId()));
        snap.put("industryCategory", product.industryCategory());
        Map<String, Object> basic = new LinkedHashMap<>();
        basic.put("businessCategory", product.businessCategory());
        basic.put("businessSubCategory", product.businessSubCategory());
        basic.put("industryCategory", product.industryCategory());
        basic.put("dataSource", product.dataSource());
        basic.put("updateFrequency", product.updateFrequency());
        basic.put("deliveryMethod", product.deliveryMethod());
        basic.put("involvesPersonalInfo", product.involvesPersonalInfo());
        basic.put("involvesPublicData", product.involvesPublicData());
        basic.put("billingMethod", product.billingMethod());
        basic.put("price", product.price());
        snap.put("basicInfo", basic);
        snap.put(
                "supplierInfo",
                Map.of(
                        "supplierName", nullToEmpty(product.supplierName()),
                        "supplierCreditCode", nullToEmpty(product.supplierCreditCode())));
        snap.put(
                "propertyRights", Map.of("propertyRightsType", nullToEmpty(product.propertyRightsType())));
        if (product.typeSpecific() != null) {
            snap.put("typeSpecific", new LinkedHashMap<>(product.typeSpecific()));
        } else {
            snap.put("typeSpecific", Map.of());
        }
        snap.put("tags", product.tags() == null ? List.of() : new ArrayList<>(product.tags()));
        snap.put("summary", product.summary());
        snap.put("scenario", product.scenario());
        snap.put("versionNo", versionNo);
        return snap;
    }

    private Map<String, String> categoryPathParts(String l3CategoryId) {
        List<String> labels = catalog.resolvePathLabels(l3CategoryId);
        Map<String, String> parts = new LinkedHashMap<>();
        parts.put("l1", labels.size() > 0 ? labels.get(0) : "");
        parts.put("l2", labels.size() > 1 ? labels.get(1) : "");
        parts.put("l3", labels.size() > 2 ? labels.get(2) : "");
        return parts;
    }

    private ValidatedWrite validateWrite(Map<String, Object> body, String updatingProductId) {
        if (body == null) {
            throw new BusinessException(400, "ERR_VALIDATION", "请求体不能为空", null);
        }
        String productName = stringVal(body.get("productName"), "").trim();
        String productCode = stringVal(body.get("productCode"), "").trim();
        String productType = stringVal(body.get("productType"), "").trim();
        String industryCategoryRaw = stringVal(body.get("industryCategory"), "").trim();
        String l3CategoryId = stringVal(body.get("l3CategoryId"), "").trim();

        if (productName.isEmpty() || productCode.isEmpty()) {
            throw new BusinessException(400, "ERR_VALIDATION", "产品名称与产品编码为必填项", null);
        }
        if (!PRODUCT_CODE.matcher(productCode).matches()) {
            throw new BusinessException(
                    400, "ERR_PRODUCT_CODE_FORMAT", "产品编码格式非法，须符合 {DOMAIN}-{FEATURE}-{NNNN}", null);
        }
        Optional<CatalogProduct> byCode = catalog.findProductByCode(productCode);
        if (byCode.isPresent()
                && (updatingProductId == null || !byCode.get().id().equals(updatingProductId))) {
            throw new BusinessException(409, "ERR_PRODUCT_CODE_CONFLICT", "产品编码已存在", null);
        }
        if (!List.of("DATASET", "REPORT", "API", "OTHER").contains(productType)) {
            throw new BusinessException(400, "ERR_VALIDATION", "产品类型非法", null);
        }

        if (industryCategoryRaw.isEmpty()) {
            throw new BusinessException(400, "ERR_VALIDATION", "行业分类为必填项", null);
        }
        String industryCategory = IndustryCategories.normalizeOrNull(industryCategoryRaw);
        if (industryCategory == null) {
            throw new BusinessException(
                    400, "ERR_VALIDATION", "行业分类非法，须为 GB/T 4754 门类枚举：" + industryCategoryRaw, null);
        }

        String l2CategoryId = null;
        String l1CategoryId = null;
        if (!l3CategoryId.isEmpty()) {
            Optional<CatalogCategory> l3 = catalog.findCategory(l3CategoryId);
            if (l3.isEmpty() || !"L3".equals(l3.get().level())) {
                throw new BusinessException(
                        400, "ERR_CATEGORY_LEAF_REQUIRED", "产品须挂载三级分类节点", null);
            }
            l2CategoryId = catalog.resolveL2IdFromL3(l3CategoryId);
            l1CategoryId = catalog.resolveL1IdFromL3(l3CategoryId);
            if (l2CategoryId == null || l1CategoryId == null) {
                throw new BusinessException(
                        400, "ERR_CATEGORY_LEAF_REQUIRED", "产品须挂载三级分类节点", null);
            }
        } else {
            l3CategoryId = null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> typeSpecific =
                body.get("typeSpecific") instanceof Map<?, ?> raw
                        ? new LinkedHashMap<>((Map<String, Object>) raw)
                        : new LinkedHashMap<>();

        typeSpecific = normalizeTypeSpecific(productType, typeSpecific);

        @SuppressWarnings("unchecked")
        List<String> tags =
                body.get("tags") instanceof List<?> list
                        ? list.stream().map(String::valueOf).map(String::trim).filter(s -> !s.isEmpty()).toList()
                        : List.of();

        return new ValidatedWrite(
                productCode,
                productName,
                productType,
                industryCategory,
                l3CategoryId,
                l2CategoryId,
                l1CategoryId,
                nullableString(body.get("businessCategory")),
                nullableString(body.get("businessSubCategory")),
                nullableString(body.get("dataSource")),
                nullableString(body.get("updateFrequency")),
                nullableString(body.get("deliveryMethod")),
                boolOr(body.get("involvesPersonalInfo")),
                boolOr(body.get("involvesPublicData")),
                nullableString(body.get("billingMethod")),
                nullableString(body.get("price")),
                nullableString(body.get("supplierName")),
                nullableString(body.get("supplierCreditCode")),
                nullableString(body.get("propertyRightsType")),
                nullableString(body.get("summary")),
                nullableString(body.get("scenario")),
                new ArrayList<>(tags),
                typeSpecific);
    }

    private static Map<String, Object> normalizeTypeSpecific(
            String productType, Map<String, Object> raw) {
        Map<String, Object> out = new LinkedHashMap<>();
        String key =
                switch (productType) {
                    case "DATASET" -> "dataset";
                    case "REPORT" -> "report";
                    case "API" -> "api";
                    case "OTHER" -> "other";
                    default -> null;
                };
        if (key == null) {
            return out;
        }
        Object section = raw.get(key);
        Map<String, Object> typed = new LinkedHashMap<>();
        if (section instanceof Map<?, ?> m) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) m;
            typed.putAll(cast);
        }
        if ("api".equals(key)) {
            typed = normalizeApiSection(typed);
        } else if ("dataset".equals(key)) {
            typed = keepKeys(
                    typed,
                    List.of(
                            "timeRange",
                            "regionScope",
                            "dataScale",
                            "dataForm",
                            "fieldDescription",
                            "dataSample"));
        } else {
            // report / other
            typed = keepKeys(typed, List.of("timeRange", "regionScope", "contentDescription"));
        }
        out.put(key, typed);
        return out;
    }

    /**
     * API：保留客户端 swaggerFileContent + endpoints；对齐 §3.3.3 — 二者均非空且不一致时
     * <b>不以</b>原文重算覆盖 endpoints。
     */
    private static Map<String, Object> normalizeApiSection(Map<String, Object> typed) {
        Map<String, Object> api = new LinkedHashMap<>();
        Object swagger = typed.get("swaggerFileContent");
        if (swagger != null) {
            api.put("swaggerFileContent", String.valueOf(swagger));
        }
        List<Map<String, Object>> endpoints = new ArrayList<>();
        Object epRaw = typed.get("endpoints");
        if (epRaw instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> m) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ep = new LinkedHashMap<>((Map<String, Object>) m);
                    endpoints.add(ep);
                }
            }
        }
        api.put("endpoints", endpoints);
        for (String k :
                List.of("fieldDescription", "dataSample", "timeRange", "regionScope", "endpoint")) {
            if (typed.get(k) != null) {
                api.put(k, typed.get(k));
            }
        }
        // 兼容字段：若未提供 endpoint 且有 endpoints，派生首端点 method path
        if (!api.containsKey("endpoint") && !endpoints.isEmpty()) {
            Map<String, Object> first = endpoints.get(0);
            api.put(
                    "endpoint",
                    String.valueOf(first.getOrDefault("method", ""))
                            + " "
                            + String.valueOf(first.getOrDefault("path", "")));
        }
        return api;
    }

    private static Map<String, Object> keepKeys(Map<String, Object> src, List<String> keys) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String k : keys) {
            if (src.get(k) != null) {
                out.put(k, src.get(k));
            }
        }
        return out;
    }

    private CatalogProduct toProduct(
            String id, ValidatedWrite w, int chainCount, Integer latestVersionNo) {
        String path =
                w.l3CategoryId() == null ? null : catalog.resolveCategoryPathFromL3(w.l3CategoryId());
        return new CatalogProduct(
                id,
                w.productCode(),
                w.productName(),
                w.productType(),
                w.l2CategoryId(),
                w.l1CategoryId(),
                path,
                chainCount,
                latestVersionNo,
                w.dataSource(),
                w.deliveryMethod(),
                w.involvesPublicData(),
                w.involvesPersonalInfo(),
                w.summary(),
                w.scenario(),
                w.supplierName(),
                w.supplierCreditCode(),
                w.tags(),
                w.typeSpecific(),
                w.businessCategory(),
                w.businessSubCategory(),
                w.updateFrequency(),
                w.billingMethod(),
                w.price(),
                w.propertyRightsType(),
                w.l3CategoryId(),
                w.industryCategory(),
                Instant.now());
    }

    private static CatalogProduct withChainMeta(CatalogProduct p, int chainCount, int latestVersionNo) {
        return new CatalogProduct(
                p.id(),
                p.productCode(),
                p.productName(),
                p.productType(),
                p.l2CategoryId(),
                p.l1CategoryId(),
                p.categoryPath(),
                chainCount,
                latestVersionNo,
                p.dataSource(),
                p.deliveryMethod(),
                p.involvesPublicData(),
                p.involvesPersonalInfo(),
                p.summary(),
                p.scenario(),
                p.supplierName(),
                p.supplierCreditCode(),
                p.tags(),
                p.typeSpecific(),
                p.businessCategory(),
                p.businessSubCategory(),
                p.updateFrequency(),
                p.billingMethod(),
                p.price(),
                p.propertyRightsType(),
                p.l3CategoryId(),
                p.industryCategory(),
                Instant.now());
    }

    private Map<String, Object> toMap(CatalogProduct p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.id());
        m.put("productCode", p.productCode());
        m.put("productName", p.productName());
        m.put("productType", p.productType());
        m.put("industryCategory", p.industryCategory());
        m.put("l2CategoryId", p.l2CategoryId());
        if (p.l3CategoryId() != null) {
            m.put("l3CategoryId", p.l3CategoryId());
        }
        m.put("categoryPath", p.categoryPath());
        m.put("chainCount", p.chainCount());
        m.put("latestVersionNo", p.latestVersionNo());
        m.put("dataSource", p.dataSource());
        m.put("deliveryMethod", p.deliveryMethod());
        m.put("involvesPublicData", p.involvesPublicData());
        m.put("involvesPersonalInfo", p.involvesPersonalInfo());
        m.put("summary", p.summary());
        m.put("scenario", p.scenario());
        m.put("supplierName", p.supplierName());
        m.put("supplierCreditCode", p.supplierCreditCode());
        m.put("tags", p.tags() == null ? List.of() : new ArrayList<>(p.tags()));
        m.put("businessCategory", p.businessCategory());
        m.put("businessSubCategory", p.businessSubCategory());
        m.put("updateFrequency", p.updateFrequency());
        m.put("billingMethod", p.billingMethod());
        m.put("price", p.price());
        m.put("propertyRightsType", p.propertyRightsType());
        if (p.typeSpecific() != null && !p.typeSpecific().isEmpty()) {
            m.put("typeSpecific", new LinkedHashMap<>(p.typeSpecific()));
        }
        if (p.updatedAt() != null) {
            m.put("updatedAt", p.updatedAt().toString());
        }
        return m;
    }

    private static String stringVal(Object v, String fallback) {
        if (v == null) {
            return fallback;
        }
        return String.valueOf(v);
    }

    private static String nullableString(Object v) {
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private static Boolean boolOr(Object v) {
        if (v instanceof Boolean b) {
            return b;
        }
        if (v == null) {
            return null;
        }
        return Boolean.parseBoolean(String.valueOf(v));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private record ValidatedWrite(
            String productCode,
            String productName,
            String productType,
            String industryCategory,
            String l3CategoryId,
            String l2CategoryId,
            String l1CategoryId,
            String businessCategory,
            String businessSubCategory,
            String dataSource,
            String updateFrequency,
            String deliveryMethod,
            Boolean involvesPersonalInfo,
            Boolean involvesPublicData,
            String billingMethod,
            String price,
            String supplierName,
            String supplierCreditCode,
            String propertyRightsType,
            String summary,
            String scenario,
            List<String> tags,
            Map<String, Object> typeSpecific) {
    }

    private record Attested(int versionNo, String versionId) {
    }
}
