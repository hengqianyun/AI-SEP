package com.shdata.datachain.service.chain;

import com.shdata.datachain.common.port.ChainAttestationPort;
import com.shdata.datachain.config.ChainMpProperties;
import com.shdata.datachain.model.ChainEvidenceRequest;
import com.shdata.datachain.model.ChainEvidenceResult;
import com.shdata.datachain.model.SjmlAppendNodeRequest;
import com.shdata.datachain.model.SjmlAppendNodeResponse;
import com.shdata.datachain.model.SjmlChainSummary;
import com.shdata.datachain.model.SjmlCreateChainRequest;
import com.shdata.datachain.model.SjmlNodeSummary;
import com.shdata.datachain.repository.ChainRegistryStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

/**
 * 通用异步上链服务（溯源链模型）：后台为产品找/建溯源链、追加节点、轮询确认，重试耗尽后降级 mock。
 *
 * <p>业务模块只依赖 {@link #submitAndConfirmAsync(ChainEvidenceRequest)}，无需了解签名、建链、异步状态和降级策略。
 * 同一产品的所有版本节点共享一条溯源链（{@code product_code ↔ chain_id} 由 {@link ChainRegistryStore} 维护）。</p>
 */
@Service
public class ChainEvidenceService {

  private static final Logger log = LoggerFactory.getLogger(ChainEvidenceService.class);

  private final ChainMpClient client;
  private final ChainMpProperties properties;
  private final ChainAttestationPort mockPort;
  private final ChainRegistryStore chainRegistry;
  private final Executor taskExecutor;
  private final TaskScheduler taskScheduler;
  private final Clock clock;

  /**
   * 创建异步上链服务。
   *
   * @param client sjml API 客户端
   * @param properties 重试与凭证配置
   * @param mockPort 本地 mock 结果提供者
   * @param chainRegistry 溯源链注册表（product_code↔chain_id）
   * @param taskExecutor HTTP 调用线程池
   * @param taskScheduler 重试调度器
   */
  @Autowired
  public ChainEvidenceService(
      ChainMpClient client,
      ChainMpProperties properties,
      @Qualifier("simulatedChainAttestationPort") ChainAttestationPort mockPort,
      ChainRegistryStore chainRegistry,
      @Qualifier("chainTaskExecutor") Executor taskExecutor,
      @Qualifier("chainTaskScheduler") TaskScheduler taskScheduler) {
    this(client, properties, mockPort, chainRegistry, taskExecutor, taskScheduler, Clock.systemUTC());
  }

  /**
   * 创建可注入时间源的服务实例。
   *
   * @param client sjml API 客户端
   * @param properties 重试与凭证配置
   * @param mockPort 本地 mock 结果提供者
   * @param chainRegistry 溯源链注册表
   * @param taskExecutor HTTP 调用线程池
   * @param taskScheduler 重试调度器
   * @param clock 时间源
   */
  ChainEvidenceService(
      ChainMpClient client,
      ChainMpProperties properties,
      ChainAttestationPort mockPort,
      ChainRegistryStore chainRegistry,
      Executor taskExecutor,
      TaskScheduler taskScheduler,
      Clock clock) {
    this.client = client;
    this.properties = properties;
    this.mockPort = mockPort;
    this.chainRegistry = chainRegistry;
    this.taskExecutor = taskExecutor;
    this.taskScheduler = taskScheduler;
    this.clock = clock;
  }

  /**
   * 异步提交并等待上链确认。真实建链/追加/查询分别重试；任一阶段耗尽后返回 mock 结果。
   *
   * @param request 通用存证请求（metadata 需含 product_code）
   * @return 最终确认或 mock 降级结果的 Future
   */
  public CompletableFuture<ChainEvidenceResult> submitAndConfirmAsync(ChainEvidenceRequest request) {
    validate(request);
    String contentHash = sha256Hex(request.content());
    if (!properties.isEnabled()) {
      return fallbackFuture(request, contentHash, "ChainMP disabled", null);
    }
    return submitAttempt(request, contentHash, 1);
  }

  /** 执行一次异步建链+追加节点，失败时延迟重试；追加成功后按状态进入查询阶段或直接确认。 */
  private CompletableFuture<ChainEvidenceResult> submitAttempt(
      ChainEvidenceRequest request, String contentHash, int attempt) {
    CompletableFuture<ChainEvidenceResult> target = new CompletableFuture<>();
    CompletableFuture.supplyAsync(() -> submit(request, contentHash), taskExecutor)
        .whenComplete(
            (outcome, error) -> {
              if (error != null) {
                Throwable cause = unwrap(error);
                if (cause instanceof ChainMpApiException apiException
                    && apiException.statusCode() == 409) {
                  // 同 data_hash 已存，转查询既有节点
                  scheduleQuery(target, request, contentHash, outcomePlaceholder(request), 1);
                  return;
                }
                if (cause instanceof ChainMpApiException apiException
                    && apiException.statusCode() >= 200
                    && apiException.statusCode() < 300) {
                  log.warn(
                      "sjml submit response invalid; retry suppressed to avoid duplicate write: "
                          + "evidenceId={}, error={}",
                      request.evidenceId(),
                      describeError(cause));
                  completeFallback(
                      target,
                      request,
                      contentHash,
                      "remote accepted request but response was invalid; retry suppressed",
                      cause);
                  return;
                }
                log.warn(
                    "sjml submit failed: evidenceId={}, attempt={}/{}, error={}",
                    request.evidenceId(),
                    attempt,
                    properties.getSubmitMaxAttempts(),
                    describeError(cause));
                if (attempt < properties.getSubmitMaxAttempts()) {
                  schedule(target, () -> submitAttempt(request, contentHash, attempt + 1));
                } else {
                  completeFallback(target, request, contentHash, "submit retries exhausted", cause);
                }
                return;
              }
              if (isConfirmedStatus(outcome.node.onChainStatus())) {
                completeReal(target, request, contentHash, outcome);
              } else {
                scheduleQuery(target, request, contentHash, outcome, 1);
              }
            });
    return target;
  }

  /**
   * 找/建链并追加节点。
   *
   * @param request 通用存证请求
   * @param contentHash 本地 SHA-256
   * @return 建链+追加结果（含 chainId 与节点响应）
   */
  private SubmitOutcome submit(ChainEvidenceRequest request, String contentHash) {
    String productCode = productCodeOf(request);
    String chainId =
        chainRegistry.findChainId(productCode).orElseGet(() -> ensureChainCreated(productCode, request));
    SjmlAppendNodeResponse node = client.appendNode(chainId, buildAppendRequest(request, contentHash));
    return new SubmitOutcome(chainId, node);
  }

  /** 创建溯源链并登记映射；并发下由注册表唯一约束兜底。 */
  private String ensureChainCreated(String productCode, ChainEvidenceRequest request) {
    SjmlChainSummary created = client.createChain(buildCreateChainRequest(productCode, request));
    return chainRegistry.registerChainId(productCode, created.chainId());
  }

  /** 延迟开始一次查询尝试。 */
  private void scheduleQuery(
      CompletableFuture<ChainEvidenceResult> target,
      ChainEvidenceRequest request,
      String contentHash,
      SubmitOutcome outcome,
      int attempt) {
    schedule(target, () -> queryAttempt(request, contentHash, outcome, attempt));
  }

  /** 异步查询节点列表；找到匹配且已确认的节点即完成，否则进入下一次延迟查询。 */
  private CompletableFuture<ChainEvidenceResult> queryAttempt(
      ChainEvidenceRequest request, String contentHash, SubmitOutcome outcome, int attempt) {
    CompletableFuture<ChainEvidenceResult> target = new CompletableFuture<>();
    CompletableFuture.supplyAsync(() -> client.listNodes(outcome.chainId()), taskExecutor)
        .whenComplete(
            (nodes, error) -> {
              if (error == null) {
                SjmlNodeSummary matched = matchNode(nodes, outcome, contentHash);
                if (matched != null && isConfirmedStatus(matched.onChainStatus())) {
                  completeReal(
                      target,
                      request,
                      contentHash,
                      new SubmitOutcome(outcome.chainId(), responseFrom(matched)));
                  return;
                }
              }
              if (error != null) {
                log.warn(
                    "sjml query failed: evidenceId={}, attempt={}/{}, error={}",
                    request.evidenceId(),
                    attempt,
                    properties.getQueryMaxAttempts(),
                    describeError(unwrap(error)));
              } else {
                log.info(
                    "sjml node pending: evidenceId={}, attempt={}/{}",
                    request.evidenceId(), attempt, properties.getQueryMaxAttempts());
              }
              if (attempt < properties.getQueryMaxAttempts()) {
                scheduleQuery(target, request, contentHash, outcome, attempt + 1);
              } else {
                completeFallback(
                    target, request, contentHash, "query retries exhausted", unwrap(error));
              }
            });
    return target;
  }

  /** 按 data.content_hash 优先、sequence 次之匹配节点。 */
  private static SjmlNodeSummary matchNode(
      List<SjmlNodeSummary> nodes, SubmitOutcome outcome, String contentHash) {
    if (nodes == null || nodes.isEmpty()) {
      return null;
    }
    for (SjmlNodeSummary n : nodes) {
      String businessHash = businessContentHash(n.data());
      if (businessHash != null && contentHash.equalsIgnoreCase(businessHash)) {
        return n;
      }
    }
    Integer seq = outcome.node.sequenceNo();
    if (seq != null) {
      for (SjmlNodeSummary n : nodes) {
        if (seq.equals(n.sequenceNo())) {
          return n;
        }
      }
    }
    return null;
  }

  /** 校验平台哈希并完成真实结果。 */
  private void completeReal(
      CompletableFuture<ChainEvidenceResult> target,
      ChainEvidenceRequest request,
      String contentHash,
      SubmitOutcome outcome) {
    SjmlAppendNodeResponse node = outcome.node;
    String returnedContentHash = businessContentHash(node.data());
    if (returnedContentHash != null
        && !returnedContentHash.isBlank()
        && !contentHash.equalsIgnoreCase(returnedContentHash)) {
      log.warn("sjml hash mismatch, fallback to mock: evidenceId={}", request.evidenceId());
      completeFallback(target, request, contentHash, "content hash mismatch", null);
      return;
    }
    Instant timestamp =
        node.confirmedAt() != null && !node.confirmedAt().isBlank()
            ? parseCreatedAt(node.confirmedAt())
            : parseCreatedAt(node.createdAt());
    target.complete(
        new ChainEvidenceResult(
            request.evidenceId(),
            contentHash,
            "confirmed",
            node.transactionHash(),
            node.blockNumber(),
            timestamp,
            false,
            outcome.chainId));
  }

  /** 延迟执行下一个异步阶段，并把其完成状态转发到目标 Future。 */
  private void schedule(
      CompletableFuture<ChainEvidenceResult> target,
      Supplier<CompletableFuture<ChainEvidenceResult>> next) {
    taskScheduler.schedule(
        () -> next.get().whenComplete((value, error) -> {
          if (error == null) {
            target.complete(value);
          } else {
            target.completeExceptionally(error);
          }
        }),
        clock.instant().plusMillis(properties.getRetryDelayMillis()));
  }

  /** 根据 mock 开关完成降级；关闭 mock 时保留异常状态，禁止生成模拟凭证。 */
  private void completeFallback(
      CompletableFuture<ChainEvidenceResult> target,
      ChainEvidenceRequest request,
      String contentHash,
      String reason,
      Throwable cause) {
    fallbackFuture(request, contentHash, reason, cause)
        .whenComplete((value, error) -> {
          if (error == null) {
            target.complete(value);
          } else {
            target.completeExceptionally(unwrap(error));
          }
        });
  }

  /** 创建 mock 结果或在 mock 被关闭时创建失败 Future。 */
  private CompletableFuture<ChainEvidenceResult> fallbackFuture(
      ChainEvidenceRequest request, String contentHash, String reason, Throwable cause) {
    if (properties.isMockEnabled()) {
      log.warn(
          "Chain evidence fallback to mock: evidenceId={}, reason={}, cause={}",
          request.evidenceId(),
          reason,
          describeError(cause));
      return CompletableFuture.completedFuture(mockResult(request, contentHash));
    }
    return CompletableFuture.failedFuture(
        new IllegalStateException(
            "Chain evidence failed and mock fallback is disabled: " + reason, cause));
  }

  /** 使用现有模拟端口生成兼容的降级结果。 */
  private ChainEvidenceResult mockResult(ChainEvidenceRequest request, String contentHash) {
    Instant timestamp = clock.instant();
    try {
      ChainAttestationPort.AttestationResult mocked =
          mockPort.attest(
              new ChainAttestationPort.AttestationRequest(request.evidenceId(), 1, request.content()));
      if (mocked != null && mocked.timestamp() != null) {
        timestamp = mocked.timestamp();
      }
    } catch (RuntimeException exception) {
      // mock 本身也不能成为业务失败点；使用本服务生成的确定性凭证完成最终兜底。
      log.warn("Mock attestation provider failed, using internal fallback: evidenceId={}",
          request.evidenceId(), exception);
    }
    return new ChainEvidenceResult(
        request.evidenceId(),
        contentHash,
        "mocked",
        "mock:" + contentHash.substring(0, Math.min(32, contentHash.length())),
        null,
        timestamp,
        true,
        null);
  }

  /** 409 回查分支用的占位 outcome（chainId 未知，仅触发 listNodes 尝试）。 */
  private SubmitOutcome outcomePlaceholder(ChainEvidenceRequest request) {
    String chainId = chainRegistry.findChainId(productCodeOf(request)).orElse(null);
    return new SubmitOutcome(
        chainId,
        new SjmlAppendNodeResponse(
            null, "pending", null, null, null, null, null, null, null));
  }

  /** 取请求关联的产品编码；缺失时退回 evidenceId。 */
  private static String productCodeOf(ChainEvidenceRequest request) {
    Object v = request.metadata() == null ? null : request.metadata().get("product_code");
    String productCode = v == null ? null : String.valueOf(v);
    return (productCode == null || productCode.isBlank()) ? request.evidenceId() : productCode;
  }

  /** 构造建链请求：以产品编码为名，便于识别。 */
  private static SjmlCreateChainRequest buildCreateChainRequest(
      String productCode, ChainEvidenceRequest request) {
    return new SjmlCreateChainRequest(
        "数据产品溯源链-" + productCode,
        productCode,
        List.of("data-chain"));
  }

  /** 构造追加节点请求：节点 data 携带 content_hash 与 evidence_id；快照原文存本地，链上只存可验证哈希。 */
  private SjmlAppendNodeRequest buildAppendRequest(ChainEvidenceRequest request, String contentHash) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("content_hash", contentHash);
    data.put("evidence_id", request.evidenceId());
    String eventType =
        request.metadata() != null && request.metadata().get("event_type") != null
            ? String.valueOf(request.metadata().get("event_type"))
            : "catalog_attest";
    return new SjmlAppendNodeRequest(eventType, clock.instant().toString(), data);
  }

  /** 判断节点/链状态是否已确认。 */
  private static boolean isConfirmedStatus(String status) {
    return "confirmed".equalsIgnoreCase(status);
  }

  /** 校验通用上链参数。 */
  private static void validate(ChainEvidenceRequest request) {
    if (request == null || request.evidenceId() == null || request.evidenceId().isBlank()
        || request.content() == null) {
      throw new IllegalArgumentException("evidenceId 和 content 不能为空");
    }
  }

  /** 解析平台带或不带时区的 ISO-8601 时间。 */
  private Instant parseCreatedAt(String value) {
    if (value == null || value.isBlank()) {
      return clock.instant();
    }
    try {
      return OffsetDateTime.parse(value).toInstant();
    } catch (java.time.format.DateTimeParseException ignored) {
      return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
    }
  }

  /** 去除 CompletableFuture 包装异常。 */
  private static Throwable unwrap(Throwable error) {
    Throwable current = error;
    while (current instanceof CompletionException && current.getCause() != null) {
      current = current.getCause();
    }
    return current;
  }

  /** 从节点 data 中读取业务原文哈希；data_hash 是整个节点的哈希，不能用于该比较。 */
  private static String businessContentHash(Map<String, Object> data) {
    if (data == null) {
      return null;
    }
    Object value = data.get("content_hash");
    return value == null ? null : String.valueOf(value);
  }

  /** 将节点列表项转换为最终结果使用的完整节点响应。 */
  private static SjmlAppendNodeResponse responseFrom(SjmlNodeSummary node) {
    return new SjmlAppendNodeResponse(
        node.nodeId(),
        node.onChainStatus(),
        node.sequenceNo(),
        node.dataHash(),
        node.data(),
        node.transactionHash(),
        node.blockNumber(),
        node.createdAt(),
        node.confirmedAt());
  }

  /** 将远端异常压缩成适合重试日志的一行摘要，保留 HTTP 状态和响应正文。 */
  private static String describeError(Throwable error) {
    if (error == null) {
      return "<none>";
    }
    Throwable cause = unwrap(error);
    if (cause instanceof ChainMpApiException apiException) {
      return "HTTP " + apiException.statusCode() + " response="
          + summarizeErrorBody(apiException.responseBody());
    }
    String message = cause.getMessage();
    return cause.getClass().getSimpleName()
        + (message == null || message.isBlank() ? "" : ": " + message);
  }

  /** 脱敏并限制服务端错误正文长度。 */
  private static String summarizeErrorBody(String body) {
    if (body == null || body.isBlank()) {
      return "<empty>";
    }
    String sanitized =
        body.replaceAll("[\\r\\n\\t]+", " ")
            .replaceAll(
                "(?i)(\\\"(?:app_secret|secret|signature|token|password)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")",
                "$1***$2");
    int maxChars = 1000;
    return sanitized.length() <= maxChars
        ? sanitized
        : sanitized.substring(0, maxChars) + "...<truncated>";
  }

  /** 计算小写 SHA-256 十六进制值。 */
  private static String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 not available", exception);
    }
  }

  /** 建链+追加的中间结果，沿 CompletableFuture 管道按值传递（避免跨线程读 DB 可见性）。 */
  private record SubmitOutcome(String chainId, SjmlAppendNodeResponse node) {}
}
