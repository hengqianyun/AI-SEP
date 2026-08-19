package com.shdata.datachain.service.chain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.config.ChainMpProperties;
import com.shdata.datachain.model.SjmlAppendNodeRequest;
import com.shdata.datachain.model.SjmlAppendNodeResponse;
import com.shdata.datachain.model.SjmlChainSummary;
import com.shdata.datachain.model.SjmlCreateChainRequest;
import com.shdata.datachain.model.SjmlNodeSummary;
import com.shdata.datachain.model.SjmlPublicChainResponse;
import com.shdata.datachain.model.SjmlVerifyResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * sjml 溯源链 API 客户端，统一处理请求序列化、HMAC-SHA256 签名与错误响应。
 *
 * <p>鉴权头：{@code X-App-Id} / {@code X-Timestamp} / {@code X-Nonce} /
 * {@code X-Signature}；公开接口（{@code /public}）不带签名。
 * 响应为直接对象或数组，由 {@link #unwrap} 解析。</p>
 *
 * <p>签名串（material）= {@code timestamp + nonce + method(大写) + path + body}
 * （HMAC-SHA256 → Base64）。
 * 若联调验签不通过，仅需调整 {@link #sign} 一处。</p>
 */
@Service
public class ChainMpClient {

  private static final String CHAINS_PATH = "/api/v1/sjml/chains";
  private static final int MAX_LOG_RESPONSE_CHARS = 2000;
  private static final Logger log = LoggerFactory.getLogger(ChainMpClient.class);

  private final ChainMpProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final Clock clock;

  /**
   * 创建生产客户端。
   *
   * @param properties ChainMP/sjml 配置
   * @param objectMapper JSON 编解码器
   */
  @Autowired
  public ChainMpClient(ChainMpProperties properties, ObjectMapper objectMapper) {
    this(
        properties,
        objectMapper,
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()))
            .build(),
        Clock.systemUTC());
  }

  /**
   * 创建可测试客户端。
   *
   * @param properties 配置
   * @param objectMapper JSON 编解码器
   * @param httpClient HTTP 客户端
   * @param clock 时间源
   */
  ChainMpClient(
      ChainMpProperties properties,
      ObjectMapper objectMapper,
      HttpClient httpClient,
      Clock clock) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient = httpClient;
    this.clock = clock;
  }

  /**
   * 创建溯源链。
   *
   * @param request 建链请求（name 必填）
   * @return 建链摘要（含 chain_id）
   */
  public SjmlChainSummary createChain(SjmlCreateChainRequest request) {
    String body = send("POST", CHAINS_PATH, writeJson(request), true);
    SjmlChainSummary result = unwrap(body, new TypeReference<SjmlChainSummary>() {});
    if (result == null || result.chainId() == null || result.chainId().isBlank()) {
      log.error(
          "ChainMP create-chain response missing chain_id: response={}",
          summarizeResponse(body));
      throw new ChainMpApiException(200, body);
    }
    return result;
  }

  /**
   * 查询当前租户的溯源链列表。
   *
   * @return 链摘要列表
   */
  public List<SjmlChainSummary> listChains() {
    return unwrap(send("GET", CHAINS_PATH, "", true),
        new TypeReference<List<SjmlChainSummary>>() {});
  }

  /**
   * 向指定溯源链追加节点，生成可验证的数据哈希。
   *
   * @param chainId 溯源链 ID
   * @param request 节点请求（event_type/event_time 必填）
   * @return 节点响应（含 node_id、on_chain_status）
   */
  public SjmlAppendNodeResponse appendNode(String chainId, SjmlAppendNodeRequest request) {
    return unwrap(send("POST", nodePath(chainId), writeJson(request), true),
        new TypeReference<SjmlAppendNodeResponse>() {});
  }

  /**
   * 按序号查询指定溯源链的节点列表。
   *
   * @param chainId 溯源链 ID
   * @return 节点摘要列表
   */
  public List<SjmlNodeSummary> listNodes(String chainId) {
    return unwrap(send("GET", nodePath(chainId), "", true),
        new TypeReference<List<SjmlNodeSummary>>() {});
  }

  /**
   * 校验溯源链完整性（节点顺序、链状态、存证状态一致）。
   *
   * @param chainId 溯源链 ID
   * @return 校验结果（is_valid）
   */
  public SjmlVerifyResponse verifyChain(String chainId) {
    return unwrap(send("GET", chainSubPath(chainId, "verify"), "", true),
        new TypeReference<SjmlVerifyResponse>() {});
  }

  /**
   * 公开溯源查询（脱敏，无需 HMAC 凭证）。
   *
   * @param chainId 溯源链 ID
   * @return 公开链信息（含 nodes）
   */
  public SjmlPublicChainResponse publicChain(String chainId) {
    return unwrap(send("GET", chainSubPath(chainId, "public"), "", false),
        new TypeReference<SjmlPublicChainResponse>() {});
  }

  /**
   * 发送请求并返回原始响应体；{@code signed=false} 时跳过 HMAC 头（公开接口）。
   *
   * @param method HTTP 方法
   * @param path 请求路径（同时用于签名与 URL 拼接，须与实际发送逐字符一致）
   * @param body 原始请求体
   * @param signed 是否加签名头
   * @return 响应体字符串
   */
  private String send(String method, String path, String body, boolean signed) {
    String url = properties.getBaseUrl() + path;
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()))
            .header("Content-Type", "application/json")
            .method(
                method,
                body.isEmpty()
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
    String timestamp = null;
    String nonce = null;
    if (signed) {
      timestamp = Long.toString(clock.instant().getEpochSecond());
      nonce = UUID.randomUUID().toString().replace("-", "");
      String signature = sign(timestamp, nonce, method, path, body, properties.getAppSecret());
      builder
          .header("X-App-Id", properties.getAppId())
          .header("X-Timestamp", timestamp)
          .header("X-Nonce", nonce)
          .header("X-Signature", signature);
    }
    HttpRequest request = builder.build();
    log.info(
        "ChainMP request started: method={}, url={}, signed={}, appId={}, timestamp={}, nonce={}, requestBytes={}",
        method,
        url,
        signed,
        maskAppId(properties.getAppId()),
        timestamp,
        nonce,
        body.getBytes(StandardCharsets.UTF_8).length);
    long startedNanos = System.nanoTime();
    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      long elapsedMillis = elapsedMillis(startedNanos);
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        log.warn(
            "ChainMP request failed: method={}, url={}, status={}, elapsedMs={}, response={}",
            method,
            url,
            response.statusCode(),
            elapsedMillis,
            summarizeResponse(response.body()));
        throw new ChainMpApiException(response.statusCode(), response.body());
      }
      log.info(
          "ChainMP request completed: method={}, url={}, status={}, elapsedMs={}, responseBytes={}",
          method,
          url,
          response.statusCode(),
          elapsedMillis,
          response.body() == null
              ? 0
              : response.body().getBytes(StandardCharsets.UTF_8).length);
      return response.body();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      log.warn(
          "ChainMP request interrupted: method={}, url={}, elapsedMs={}",
          method,
          url,
          elapsedMillis(startedNanos));
      throw new IllegalStateException("sjml API 调用被中断", exception);
    } catch (IOException exception) {
      log.error(
          "ChainMP communication failed: method={}, url={}, elapsedMs={}, error={}",
          method,
          url,
          elapsedMillis(startedNanos),
          exception.toString());
      throw new IllegalStateException("sjml API 通信失败", exception);
    }
  }

  /**
   * 解析 ChainMP 当前接口直接返回的对象或数组。
   *
   * @param body 响应体
   * @param type 目标类型
   * @param <T> 泛型
   * @return 反序列化结果
   */
  private <T> T unwrap(String body, TypeReference<T> type) {
    try {
      JsonNode root = objectMapper.readTree(body);
      if (root == null || root.isMissingNode() || root.isNull()) {
        log.error(
            "ChainMP response is empty or null: response={}",
            summarizeResponse(body));
        throw new ChainMpApiException(200, body);
      }
      return objectMapper.convertValue(root, type);
    } catch (IOException | IllegalArgumentException exception) {
      log.error(
          "ChainMP response parse failed: error={}, response={}",
          exception.getMessage(),
          summarizeResponse(body));
      throw new ChainMpApiException(200, body);
    }
  }

  /** 计算请求耗时（毫秒）。 */
  private static long elapsedMillis(long startedNanos) {
    return (System.nanoTime() - startedNanos) / 1_000_000L;
  }

  /** App ID 仅保留首尾字符，避免完整凭据进入日志。 */
  private static String maskAppId(String appId) {
    if (appId == null || appId.isBlank()) {
      return "<empty>";
    }
    if (appId.length() <= 6) {
      return "***";
    }
    return appId.substring(0, 3) + "***" + appId.substring(appId.length() - 3);
  }

  /** 单行、脱敏并截断远端响应，防止异常日志过大或泄露凭据。 */
  private static String summarizeResponse(String responseBody) {
    if (responseBody == null || responseBody.isBlank()) {
      return "<empty>";
    }
    String sanitized =
        responseBody
            .replaceAll("[\\r\\n\\t]+", " ")
            .replaceAll(
                "(?i)(\\\"(?:app_secret|secret|signature|token|password)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")",
                "$1***$2");
    if (sanitized.length() <= MAX_LOG_RESPONSE_CHARS) {
      return sanitized;
    }
    return sanitized.substring(0, MAX_LOG_RESPONSE_CHARS)
        + "...<truncated,totalChars=" + sanitized.length() + ">";
  }

  /** 拼接节点路径：/chains/{chain_id}/nodes。 */
  private static String nodePath(String chainId) {
    return chainSubPath(chainId, "nodes");
  }

  /** 拼接链子路径：/chains/{chain_id}/{suffix}，chain_id 做 URL 编码。 */
  private static String chainSubPath(String chainId, String suffix) {
    if (chainId == null || chainId.isBlank()) {
      throw new IllegalArgumentException("chainId must not be blank");
    }
    String encoded = URLEncoder.encode(chainId, StandardCharsets.UTF_8).replace("+", "%20");
    return CHAINS_PATH + "/" + encoded + "/" + suffix;
  }

  /**
   * 按文档规则生成 HMAC-SHA256 Base64 签名。
   *
   * <p>签名串 = {@code timestamp + nonce + method(大写) + path + body}。
   *
   * @param timestamp Unix 秒时间戳
   * @param nonce 每次请求唯一的随机字符串
   * @param method HTTP 方法
   * @param path 请求路径（与实际发送逐字符一致）
   * @param body 原始请求体
   * @param appSecret 签名密钥
   * @return Base64 签名
   */
  static String sign(
      String timestamp, String nonce, String method, String path, String body, String appSecret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] material =
          (timestamp + nonce + method.toUpperCase() + path + body)
              .getBytes(StandardCharsets.UTF_8);
      return Base64.getEncoder().encodeToString(mac.doFinal(material));
    } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
      throw new IllegalStateException("HmacSHA256 签名失败", exception);
    }
  }

  /**
   * 序列化请求对象，生成字符串同时用于签名与发送。
   *
   * @param value 请求对象
   * @return JSON 原文
   */
  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("sjml 请求序列化失败", exception);
    }
  }
}
