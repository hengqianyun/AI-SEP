package com.shdata.datachain.catalog.editor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.catalog.browse.CatalogBrowseSeedStore;
import com.shdata.datachain.catalog.browse.CatalogCategory;
import com.shdata.datachain.catalog.browse.CatalogProduct;
import com.shdata.datachain.chain.ChainAttestationPort;
import com.shdata.datachain.chain.InMemoryChainStore;
import com.shdata.datachain.security.AuthAuditLogger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * 产品新增/编辑 + 同事务模拟存证（DEC-WSC-001/002；TASK-WSC-301 typeSpecific 2.1.0）。
 *
 * <p>适配失败时不落产品半成品、不写孤儿上链行。须挂载 L3（{@code ERR_CATEGORY_LEAF_REQUIRED}）。
 * OTHER 允许 {@code typeSpecific.other.contentDescription}；API 写冲突以客户端 endpoints 为准。
 */
@Service
public class ProductEditorService {

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

  public Result create(Map<String, Object> body, String actorUserId, String correlationId) {
    String codeHint = stringVal(body == null ? null : body.get("productCode"), "-");
    try {
      ValidatedWrite write = validateWrite(body, null);
      String productId = catalog.nextProductId();
      CatalogProduct draft = toProduct(productId, write, 0, null);
      Attested attested = attestAndStore(draft, 1);
      CatalogProduct published = withChainMeta(draft, 1, attested.versionNo());
      catalog.upsertProduct(published);
      auditLogger.productSubmit(actorUserId, published.productCode(), "SUCCESS", null, correlationId);
      return Result.ok(toMap(published));
    } catch (BusinessException ex) {
      auditLogger.productSubmit(actorUserId, codeHint, "FAILURE", ex.code, correlationId);
      return Result.fail(ex.httpStatus, ex.code, ex.message, ex.data);
    } catch (RuntimeException ex) {
      auditLogger.productSubmit(actorUserId, codeHint, "FAILURE", "ERR_ATTESTATION", correlationId);
      return Result.fail(500, "ERR_ATTESTATION", "存证适配失败，未写入产品", null);
    }
  }

  public Result update(
      String productId, Map<String, Object> body, String actorUserId, String correlationId) {
    Optional<CatalogProduct> existingOpt = catalog.findProduct(productId);
    if (existingOpt.isEmpty()) {
      return Result.fail(404, "404", "产品不存在", null);
    }
    CatalogProduct existing = existingOpt.get();
    try {
      ValidatedWrite write = validateWrite(body, productId);
      int nextVersion = Math.max(chainStore.latestVersionNo(productId), nullSafe(existing.latestVersionNo())) + 1;
      CatalogProduct draft =
          toProduct(productId, write, existing.chainCount(), existing.latestVersionNo());
      Attested attested = attestAndStore(draft, nextVersion);
      CatalogProduct published =
          withChainMeta(draft, existing.chainCount() + 1, attested.versionNo());
      catalog.upsertProduct(published);
      auditLogger.productSubmit(actorUserId, published.productCode(), "SUCCESS", null, correlationId);
      return Result.ok(toMap(published));
    } catch (BusinessException ex) {
      auditLogger.productSubmit(actorUserId, existing.productCode(), "FAILURE", ex.code, correlationId);
      return Result.fail(ex.httpStatus, ex.code, ex.message, ex.data);
    } catch (RuntimeException ex) {
      auditLogger.productSubmit(
          actorUserId, existing.productCode(), "FAILURE", "ERR_ATTESTATION", correlationId);
      return Result.fail(500, "ERR_ATTESTATION", "存证适配失败，未更新产品", null);
    }
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
        product.id(),
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
    Map<String, Object> basic = new LinkedHashMap<>();
    basic.put("businessCategory", product.businessCategory());
    basic.put("businessSubCategory", product.businessSubCategory());
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

    if (l3CategoryId.isEmpty()) {
      throw new BusinessException(
          400, "ERR_CATEGORY_LEAF_REQUIRED", "产品须挂载三级分类节点", null);
    }
    Optional<CatalogCategory> l3 = catalog.findCategory(l3CategoryId);
    if (l3.isEmpty() || !"L3".equals(l3.get().level())) {
      throw new BusinessException(
          400, "ERR_CATEGORY_LEAF_REQUIRED", "产品须挂载三级分类节点", null);
    }
    String l2CategoryId = catalog.resolveL2IdFromL3(l3CategoryId);
    String l1CategoryId = catalog.resolveL1IdFromL3(l3CategoryId);
    if (l2CategoryId == null || l1CategoryId == null) {
      throw new BusinessException(
          400, "ERR_CATEGORY_LEAF_REQUIRED", "产品须挂载三级分类节点", null);
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
    String path = catalog.resolveCategoryPathFromL3(w.l3CategoryId());
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
        Instant.now());
  }

  private Map<String, Object> toMap(CatalogProduct p) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", p.id());
    m.put("productCode", p.productCode());
    m.put("productName", p.productName());
    m.put("productType", p.productType());
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
      Map<String, Object> typeSpecific) {}

  private record Attested(int versionNo, String versionId) {}

  static final class BusinessException extends RuntimeException {
    final int httpStatus;
    final String code;
    final String message;
    final Map<String, Object> data;

    BusinessException(int httpStatus, String code, String message, Map<String, Object> data) {
      super(message);
      this.httpStatus = httpStatus;
      this.code = code;
      this.message = message;
      this.data = data;
    }
  }

  public record Result(boolean ok, int httpStatus, String code, String message, Object data) {
    static Result ok(Object data) {
      return new Result(true, 200, "0", "ok", data);
    }

    static Result fail(int httpStatus, String code, String message, Object data) {
      return new Result(false, httpStatus, code, message, data);
    }
  }
}
