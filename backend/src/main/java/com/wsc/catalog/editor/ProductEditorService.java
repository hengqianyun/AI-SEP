package com.wsc.catalog.editor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsc.catalog.browse.CatalogBrowseSeedStore;
import com.wsc.catalog.browse.CatalogCategory;
import com.wsc.catalog.browse.CatalogProduct;
import com.wsc.chain.ChainAttestationPort;
import com.wsc.chain.InMemoryChainStore;
import com.wsc.security.AuthAuditLogger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * 产品新增/编辑 + 同事务模拟存证（DEC-WSC-001/002，OQ-004）。
 *
 * <p>适配失败时不落产品半成品、不写孤儿上链行。
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
    if ("OTHER".equals(product.productType())) {
      snap.put("typeSpecific", Map.of());
    } else if (product.typeSpecific() != null) {
      snap.put("typeSpecific", new LinkedHashMap<>(product.typeSpecific()));
    }
    snap.put("tags", product.tags() == null ? List.of() : new ArrayList<>(product.tags()));
    snap.put("summary", product.summary());
    snap.put("scenario", product.scenario());
    snap.put("versionNo", versionNo);
    return snap;
  }

  private ValidatedWrite validateWrite(Map<String, Object> body, String updatingProductId) {
    if (body == null) {
      throw new BusinessException(400, "ERR_VALIDATION", "请求体不能为空", null);
    }
    String productName = stringVal(body.get("productName"), "").trim();
    String productCode = stringVal(body.get("productCode"), "").trim();
    String productType = stringVal(body.get("productType"), "").trim();
    String l2CategoryId = stringVal(body.get("l2CategoryId"), "").trim();

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
    Optional<CatalogCategory> l2 = catalog.findCategory(l2CategoryId);
    if (l2.isEmpty() || !"L2".equals(l2.get().level())) {
      throw new BusinessException(400, "ERR_VALIDATION", "二级行业分类不存在", null);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> typeSpecific =
        body.get("typeSpecific") instanceof Map<?, ?> raw
            ? new LinkedHashMap<>((Map<String, Object>) raw)
            : new LinkedHashMap<>();

    if ("OTHER".equals(productType)) {
      if (hasTypeSpecificPayload(typeSpecific)) {
        throw new BusinessException(
            400, "ERR_PRODUCT_CODE_FORMAT", "其他数据产品不得携带类型专属字段", null);
      }
      typeSpecific = new LinkedHashMap<>();
    } else {
      typeSpecific = normalizeTypeSpecific(productType, typeSpecific);
    }

    @SuppressWarnings("unchecked")
    List<String> tags =
        body.get("tags") instanceof List<?> list
            ? list.stream().map(String::valueOf).map(String::trim).filter(s -> !s.isEmpty()).toList()
            : List.of();

    return new ValidatedWrite(
        productCode,
        productName,
        productType,
        l2CategoryId,
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

  private static boolean hasTypeSpecificPayload(Map<String, Object> typeSpecific) {
    for (String key : List.of("dataset", "report", "api")) {
      Object v = typeSpecific.get(key);
      if (v instanceof Map<?, ?> m && !m.isEmpty()) {
        return true;
      }
      if (v != null && !(v instanceof Map<?, ?>)) {
        return true;
      }
    }
    return false;
  }

  private static Map<String, Object> normalizeTypeSpecific(
      String productType, Map<String, Object> raw) {
    Map<String, Object> out = new LinkedHashMap<>();
    String key =
        switch (productType) {
          case "DATASET" -> "dataset";
          case "REPORT" -> "report";
          case "API" -> "api";
          default -> null;
        };
    if (key == null) {
      return out;
    }
    Object section = raw.get(key);
    if (section instanceof Map<?, ?> m) {
      @SuppressWarnings("unchecked")
      Map<String, Object> typed = (Map<String, Object>) m;
      out.put(key, new LinkedHashMap<>(typed));
    } else {
      out.put(key, new LinkedHashMap<>());
    }
    return out;
  }

  private CatalogProduct toProduct(
      String id, ValidatedWrite w, int chainCount, Integer latestVersionNo) {
    String l1 = catalog.resolveL1Id(w.l2CategoryId());
    String path = catalog.resolveCategoryPath(w.l2CategoryId());
    return new CatalogProduct(
        id,
        w.productCode(),
        w.productName(),
        w.productType(),
        w.l2CategoryId(),
        l1,
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
        w.propertyRightsType());
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
        p.propertyRightsType());
  }

  private Map<String, Object> toMap(CatalogProduct p) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", p.id());
    m.put("productCode", p.productCode());
    m.put("productName", p.productName());
    m.put("productType", p.productType());
    m.put("l2CategoryId", p.l2CategoryId());
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
      String l2CategoryId,
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
