package com.wsc.chain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * V1 模拟存证适配（DEC-WSC-002）。无真实链 SDK；作为 Spring Bean 供 TASK-WSC-005 注入。
 */
@Component
public class SimulatedChainAttestationPort implements ChainAttestationPort {

  private final Clock clock;

  public SimulatedChainAttestationPort() {
    this(Clock.systemUTC());
  }

  /** 测试可注入固定 Clock。 */
  SimulatedChainAttestationPort(Clock clock) {
    this.clock = clock;
  }

  @Override
  public AttestationResult attest(AttestationRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("AttestationRequest must not be null");
    }
    String productCode = nullToEmpty(request.productCode());
    String snapshot = nullToEmpty(request.catalogSnapshotJson());
    int versionNo = request.versionNo();

    String material = productCode + "|" + versionNo + "|" + snapshot;
    String metadataHash = "sha256:" + sha256Hex(material);
    String ownerDID = "did:wsc:sim:" + shortFingerprint(productCode.isEmpty() ? "unknown" : productCode);
    Instant timestamp = Instant.now(clock);
    String owner = "模拟权属方-" + (productCode.isEmpty() ? "UNKNOWN" : productCode);

    return new AttestationResult(metadataHash, ownerDID, timestamp, new AttestationResult.Certificate(owner));
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  private static String sha256Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  /** 日志可记的短指纹（非完整 DID）。 */
  private static String shortFingerprint(String productCode) {
    String hex = sha256Hex(productCode);
    return hex.substring(0, Math.min(16, hex.length()));
  }
}
