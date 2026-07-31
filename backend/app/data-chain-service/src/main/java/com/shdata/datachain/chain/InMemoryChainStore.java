package com.shdata.datachain.chain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * V1 内存上链版本与目录快照。产品无记录时列表为空；未知 versionId 无快照。
 *
 * <p>口径：未知 productId → 空列表（HTTP 200），不 404。
 */
@Component
public class InMemoryChainStore {

  /** 演示种子产品：多版本 + snapshot_json。 */
  public static final String SEED_PRODUCT_ID = "prod-demo-chain-001";

  private final ObjectMapper objectMapper;
  private final Map<String, List<StoredVersion>> byProduct = new ConcurrentHashMap<>();
  private final Map<String, StoredVersion> byVersionId = new ConcurrentHashMap<>();

  public InMemoryChainStore(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    seed();
  }

  /** 最新优先（versionNo 降序）。 */
  public List<StoredVersion> listByProductId(String productId) {
    List<StoredVersion> list = byProduct.getOrDefault(productId, List.of());
    return list.stream()
        .sorted(Comparator.comparingInt(StoredVersion::versionNo).reversed())
        .toList();
  }

  public Optional<StoredVersion> findByVersionId(String versionId) {
    return Optional.ofNullable(byVersionId.get(versionId));
  }

  /**
   * SCOPE_AMEND（TASK-WSC-005）：写入新产品/编辑产生的上链版本；只读 API 不变。
   *
   * @return 已存储版本
   */
  public synchronized StoredVersion appendVersion(
      String versionId,
      String productId,
      int versionNo,
      String metadataHash,
      String ownerDID,
      Instant timestamp,
      String certOwner,
      String snapshotJson) {
    StoredVersion stored =
        stored(
            versionId,
            productId,
            versionNo,
            metadataHash,
            ownerDID,
            timestamp,
            certOwner,
            snapshotJson);
    byProduct.computeIfAbsent(productId, k -> new ArrayList<>()).add(stored);
    byVersionId.put(versionId, stored);
    return stored;
  }

  /** 当前产品最大版本号；无记录返回 0。 */
  public int latestVersionNo(String productId) {
    return listByProductId(productId).stream().mapToInt(StoredVersion::versionNo).max().orElse(0);
  }

  private void seed() {
    Instant t1 = Instant.parse("2026-07-20T08:00:00Z");
    Instant t2 = Instant.parse("2026-07-22T10:30:00Z");
    Instant t3 = Instant.parse("2026-07-25T14:15:00Z");

    StoredVersion v1 =
        stored(
            "cv-seed-001-v1",
            SEED_PRODUCT_ID,
            1,
            "sha256:1111111111111111111111111111111111111111111111111111111111111111",
            "did:wsc:sim:seed000000000001",
            t1,
            "模拟权属方-WSC-DEMO-001",
            snapshotJson("cv-seed-001-v1", 1, t1));
    StoredVersion v2 =
        stored(
            "cv-seed-001-v2",
            SEED_PRODUCT_ID,
            2,
            "sha256:2222222222222222222222222222222222222222222222222222222222222222",
            "did:wsc:sim:seed000000000001",
            t2,
            "模拟权属方-WSC-DEMO-001",
            snapshotJson("cv-seed-001-v2", 2, t2));
    StoredVersion v3 =
        stored(
            "cv-seed-001-v3",
            SEED_PRODUCT_ID,
            3,
            "sha256:3333333333333333333333333333333333333333333333333333333333333333",
            "did:wsc:sim:seed000000000001",
            t3,
            "模拟权属方-WSC-DEMO-001",
            snapshotJson("cv-seed-001-v3", 3, t3));

    List<StoredVersion> versions = List.of(v1, v2, v3);
    byProduct.put(SEED_PRODUCT_ID, new ArrayList<>(versions));
    for (StoredVersion v : versions) {
      byVersionId.put(v.versionId(), v);
    }
  }

  private StoredVersion stored(
      String versionId,
      String productId,
      int versionNo,
      String metadataHash,
      String ownerDID,
      Instant timestamp,
      String certOwner,
      String snapshotJson) {
    Map<String, Object> snapshot = parseSnapshot(snapshotJson);
    return new StoredVersion(
        versionId, productId, versionNo, metadataHash, ownerDID, timestamp, certOwner, snapshotJson, snapshot);
  }

  private Map<String, Object> parseSnapshot(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("seed snapshot_json invalid", e);
    }
  }

  private String snapshotJson(String versionId, int versionNo, Instant capturedAt) {
    try {
      Map<String, Object> snap = new LinkedHashMap<>();
      snap.put("versionId", versionId);
      snap.put("productCode", "WSC-DEMO-001");
      snap.put("productName", "演示数据产品（上链）");
      snap.put("productType", "DATASET");
      snap.put("categoryPath", "金融服务 / 征信评估 / 征信评分");
      snap.put(
          "categoryPathParts",
          Map.of("l1", "金融服务", "l2", "征信评估", "l3", "征信评分"));
      snap.put(
          "basicInfo",
          Map.of(
              "dataSource", "内部系统",
              "updateFrequency", "日更",
              "deliveryMethod", "API"));
      snap.put(
          "supplierInfo",
          Map.of("supplierName", "演示供应商", "supplierCreditCode", "91310000MA1KXXXX0X"));
      snap.put("propertyRights", Map.of("propertyRightsType", "数据使用权"));
      snap.put("typeSpecific", Map.of("datasetFormat", "CSV", "recordCount", 1000 + versionNo));
      snap.put("tags", List.of("演示", "上链", "v" + versionNo));
      snap.put("summary", "TASK-WSC-006 内存种子快照 v" + versionNo);
      snap.put("scenario", "接入端工作台试点验收");
      snap.put("capturedAt", capturedAt.toString());
      String hash =
          switch (versionNo) {
            case 1 -> "sha256:1111111111111111111111111111111111111111111111111111111111111111";
            case 2 -> "sha256:2222222222222222222222222222222222222222222222222222222222222222";
            default -> "sha256:3333333333333333333333333333333333333333333333333333333333333333";
          };
      Map<String, Object> attestation = new LinkedHashMap<>();
      attestation.put("metadataHash", hash);
      attestation.put("ownerDID", "did:wsc:sim:seed000000000001");
      attestation.put("timestamp", capturedAt.toString());
      attestation.put("certificate", Map.of("owner", "模拟权属方-WSC-DEMO-001"));
      snap.put("attestation", attestation);
      return objectMapper.writeValueAsString(snap);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  public record StoredVersion(
      String versionId,
      String productId,
      int versionNo,
      String metadataHash,
      String ownerDID,
      Instant timestamp,
      String certOwner,
      String snapshotJson,
      Map<String, Object> snapshot) {}
}
