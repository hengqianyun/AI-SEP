package com.shdata.datachain.chain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** 适配单元：输出字段对齐 DEC-WSC-002 / ChainAttestationPort。 */
class SimulatedChainAttestationPortTest {

  private final Clock fixed =
      Clock.fixed(Instant.parse("2026-07-29T08:00:00Z"), ZoneOffset.UTC);
  private final SimulatedChainAttestationPort port = new SimulatedChainAttestationPort(fixed);

  @Test
  void attest_returnsRequiredFields() {
    ChainAttestationPort.AttestationResult result =
        port.attest(
            new ChainAttestationPort.AttestationRequest(
                "WSC-DEMO-001", 1, "{\"productCode\":\"WSC-DEMO-001\"}"));

    assertNotNull(result.metadataHash());
    assertTrue(result.metadataHash().startsWith("sha256:"));
    assertFalse(result.metadataHash().isBlank());

    assertNotNull(result.ownerDID());
    assertTrue(result.ownerDID().startsWith("did:wsc:sim:"));

    assertEquals(Instant.parse("2026-07-29T08:00:00Z"), result.timestamp());

    assertNotNull(result.certificate());
    assertNotNull(result.certificate().owner());
    assertFalse(result.certificate().owner().isBlank());
    assertTrue(result.certificate().owner().contains("WSC-DEMO-001"));
  }

  @Test
  void attest_sameInput_isDeterministicForHashAndDid() {
    var req =
        new ChainAttestationPort.AttestationRequest("CODE-A", 2, "{\"n\":1}");
    var a = port.attest(req);
    var b = port.attest(req);
    assertEquals(a.metadataHash(), b.metadataHash());
    assertEquals(a.ownerDID(), b.ownerDID());
    assertEquals(a.certificate().owner(), b.certificate().owner());
  }

  @Test
  void attest_implementsPortBeanContract() {
    assertTrue(port instanceof ChainAttestationPort);
  }
}
