package com.wsc.chain;

import java.time.Instant;

/**
 * 存证适配端口 — 对齐 contracts/chain/ChainAttestationPort.md / DEC-WSC-002。
 * 业务层仅依赖本接口；V1 实现由后续 TASK-WSC-006 提供。
 */
public interface ChainAttestationPort {

  AttestationResult attest(AttestationRequest request);

  record AttestationRequest(String productCode, int versionNo, String catalogSnapshotJson) {}

  record AttestationResult(
      String metadataHash, String ownerDID, Instant timestamp, Certificate certificate) {
    public record Certificate(String owner) {}
  }
}
