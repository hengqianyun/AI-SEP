package com.shdata.datachain.service.chain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shdata.datachain.common.port.ChainAttestationPort;
import com.shdata.datachain.config.ChainMpProperties;
import com.shdata.datachain.model.ChainEvidenceRequest;
import com.shdata.datachain.model.SjmlAppendNodeResponse;
import com.shdata.datachain.model.SjmlNodeSummary;
import com.shdata.datachain.repository.ChainRegistryStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;

/** 通用异步上链编排测试（溯源链模型）。 */
class ChainEvidenceServiceTest {

  private static final Instant FIXED_TIME = Instant.parse("2026-08-11T06:00:00Z");

  private final ChainMpClient client = mock(ChainMpClient.class);
  private final ChainAttestationPort mockPort = mock(ChainAttestationPort.class);
  private final ChainRegistryStore chainRegistry = mock(ChainRegistryStore.class);
  private final TaskScheduler scheduler = mock(TaskScheduler.class);
  private final Executor directExecutor = Runnable::run;
  private final ChainMpProperties properties = new ChainMpProperties();

  /** 让延迟任务立即执行，并准备固定 mock 返回。 */
  @BeforeEach
  void setUp() {
    properties.setEnabled(true);
    properties.setAppId("test-app");
    properties.setAppSecret("test-secret");
    properties.setSubmitMaxAttempts(3);
    properties.setQueryMaxAttempts(3);
    properties.setRetryDelayMillis(1);
    doAnswer(
            invocation -> {
              invocation.getArgument(0, Runnable.class).run();
              return null;
            })
        .when(scheduler)
        .schedule(any(Runnable.class), any(Instant.class));
    when(mockPort.attest(any()))
        .thenReturn(
            new ChainAttestationPort.AttestationResult(
                "sha256:mock",
                "did:wsc:sim:test",
                FIXED_TIME,
                new ChainAttestationPort.AttestationResult.Certificate("mock-owner")));
    // 默认视为已有链，跳过建链；各用例可覆盖。
    when(chainRegistry.findChainId(any())).thenReturn(Optional.of("chain-1"));
  }

  /** 追加节点连续失败达到上限后返回 mock，不再查询。 */
  @Test
  void submitAndConfirmAsync_submitRetriesExhausted_returnsMock() {
    when(client.appendNode(eq("chain-1"), any())).thenThrow(new IllegalStateException("network down"));
    ChainEvidenceService service = service();

    var result = service.submitAndConfirmAsync(request("submit-fail")).join();

    assertTrue(result.mocked());
    assertEquals("mocked", result.status());
    verify(client, times(3)).appendNode(eq("chain-1"), any());
    verify(client, times(0)).listNodes(any());
  }

  /** 追加返回 pending，查询到同哈希 confirmed 节点时返回真实结果。 */
  @Test
  void submitAndConfirmAsync_queryConfirmed_returnsRealResult() {
    String hash = sha256("query-confirmed");
    when(client.appendNode(eq("chain-1"), any()))
        .thenReturn(node(hash, "pending", "0xtx", 1));
    when(client.listNodes("chain-1"))
        .thenReturn(
            List.of(
                new SjmlNodeSummary(
                    "n1",
                    1,
                    "confirmed",
                    "node-hash",
                    Map.of("content_hash", hash),
                    "0xtx",
                    123L,
                    "2026-08-11T06:00:05Z",
                    "2026-08-11T06:00:00Z")));
    ChainEvidenceService service = service();

    var result = service.submitAndConfirmAsync(request("query-confirmed")).join();

    assertFalse(result.mocked());
    assertEquals("confirmed", result.status());
    assertEquals("chain-1", result.chainId());
    assertEquals("0xtx", result.transactionHash());
    verify(client, times(1)).appendNode(eq("chain-1"), any());
  }

  /** 真实接口的 data_hash 是节点哈希，应使用 data.content_hash 匹配并采用查询结果。 */
  @Test
  void submitAndConfirmAsync_realNodeFields_returnsConfirmedResult() {
    String hash = sha256("real-node-fields");
    when(client.appendNode(eq("chain-1"), any()))
        .thenReturn(
            new SjmlAppendNodeResponse(
                "n1",
                "pending",
                1,
                "different-node-hash",
                Map.of("content_hash", hash),
                null,
                null,
                "2026-08-11T06:00:00Z",
                null));
    when(client.listNodes("chain-1"))
        .thenReturn(
            List.of(
                new SjmlNodeSummary(
                    "n1",
                    1,
                    "confirmed",
                    "different-node-hash",
                    Map.of("content_hash", hash),
                    "0x-real",
                    1454L,
                    "2026-08-11T06:00:05Z",
                    "2026-08-11T06:00:00Z")));
    ChainEvidenceService service = service();

    var result = service.submitAndConfirmAsync(request("real-node-fields")).join();

    assertFalse(result.mocked());
    assertEquals("confirmed", result.status());
    assertEquals("0x-real", result.transactionHash());
    assertEquals(1454L, result.blockNumber());
    assertEquals(Instant.parse("2026-08-11T06:00:05Z"), result.timestamp());
  }

  /** 追加返回 pending 且查询持续未确认，查询耗尽后返回 mock。 */
  @Test
  void submitAndConfirmAsync_queryPendingExhausted_returnsMock() {
    String hash = sha256("query-pending");
    when(client.appendNode(eq("chain-1"), any())).thenReturn(node(hash, "pending", null, 1));
    when(client.listNodes("chain-1"))
        .thenReturn(
            List.of(
                new SjmlNodeSummary(
                    "n1",
                    1,
                    "pending",
                    "node-hash",
                    Map.of("content_hash", hash),
                    null,
                    null,
                    null,
                    "2026-08-11T06:00:00Z")));
    ChainEvidenceService service = service();

    var result = service.submitAndConfirmAsync(request("query-pending")).join();

    assertTrue(result.mocked());
    verify(client, times(1)).appendNode(eq("chain-1"), any());
    verify(client, times(3)).listNodes("chain-1");
  }

  /** mock 开关关闭时，重试耗尽必须失败而非伪造结果。 */
  @Test
  void submitAndConfirmAsync_mockDisabled_returnsFailure() {
    properties.setMockEnabled(false);
    when(client.appendNode(eq("chain-1"), any())).thenThrow(new IllegalStateException("network down"));
    ChainEvidenceService service = service();

    CompletionException exception =
        assertThrows(
            CompletionException.class,
            () -> service.submitAndConfirmAsync(request("mock-disabled")).join());

    assertTrue(exception.getCause().getMessage().contains("mock fallback is disabled"));
    verify(client, times(3)).appendNode(eq("chain-1"), any());
    verify(mockPort, times(0)).attest(any());
  }

  /** 产品无链时先建链再追加。 */
  @Test
  void submitAndConfirmAsync_createsChainWhenAbsent() {
    when(chainRegistry.findChainId(any())).thenReturn(Optional.empty());
    when(client.createChain(any()))
        .thenReturn(new com.shdata.datachain.model.SjmlChainSummary("chain-new", "confirmed", null, null));
    when(chainRegistry.registerChainId(any(), eq("chain-new"))).thenReturn("chain-new");
    String hash = sha256("create-chain");
    when(client.appendNode(eq("chain-new"), any())).thenReturn(node(hash, "confirmed", "0xtx", 1));
    ChainEvidenceService service = service();

    var result = service.submitAndConfirmAsync(request("create-chain")).join();

    assertFalse(result.mocked());
    assertEquals("chain-new", result.chainId());
    verify(client, times(1)).createChain(any());
    verify(client, times(1)).appendNode(eq("chain-new"), any());
  }

  /** 远端建链已返回 2xx 但响应不可解析时不得重试，避免重复创建链。 */
  @Test
  void submitAndConfirmAsync_createChainResponseInvalid_doesNotRetry() {
    when(chainRegistry.findChainId(any())).thenReturn(Optional.empty());
    when(client.createChain(any()))
        .thenThrow(new ChainMpApiException(200, "{\"status\":\"active\"}"));
    ChainEvidenceService service = service();

    var result = service.submitAndConfirmAsync(request("invalid-create-response")).join();

    assertTrue(result.mocked());
    verify(client, times(1)).createChain(any());
    verify(client, times(0)).appendNode(any(), any());
  }

  /** 创建被测服务。 */
  private ChainEvidenceService service() {
    return new ChainEvidenceService(
        client,
        properties,
        mockPort,
        chainRegistry,
        directExecutor,
        scheduler,
        Clock.fixed(FIXED_TIME, ZoneOffset.UTC));
  }

  /** 创建通用请求（metadata 含 product_code）。 */
  private static ChainEvidenceRequest request(String content) {
    return new ChainEvidenceRequest("EV-" + content, content, Map.of("product_code", "P-1"));
  }

  /** 构造追加节点响应。 */
  private static SjmlAppendNodeResponse node(
      String hash, String status, String txHash, int seq) {
    return new SjmlAppendNodeResponse(
        "n1",
        status,
        seq,
        "node-hash",
        Map.of("content_hash", hash),
        txHash,
        123L,
        "2026-08-11T06:00:00Z",
        "confirmed".equals(status) ? "2026-08-11T06:00:05Z" : null);
  }

  /** 计算测试原文 SHA-256。 */
  private static String sha256(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      throw new IllegalStateException(exception);
    }
  }
}
