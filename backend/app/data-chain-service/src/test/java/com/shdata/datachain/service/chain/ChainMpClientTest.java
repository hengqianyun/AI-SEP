package com.shdata.datachain.service.chain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.config.ChainMpProperties;
import com.shdata.datachain.model.SjmlChainSummary;
import com.shdata.datachain.model.SjmlCreateChainRequest;
import com.shdata.datachain.model.SjmlNodeSummary;
import com.shdata.datachain.model.SjmlPublicChainResponse;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * sjml 溯源链客户端单元测试：签名 material = timestamp+nonce+method+path+body、信封解包、公开接口不签名。
 */
class ChainMpClientTest {

  private HttpServer server;

  /** 停止测试 HTTP 服务。 */
  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  /** 校验签名 material 为 timestamp+nonce+method+path+body。 */
  @Test
  void sign_conformsToTimestampMethodPathBodyMaterial() throws Exception {
    String ts = "1721894400";
    String nonce = "a1b2c3d4e5f678901234567890abcdef";
    String method = "POST";
    String path = "/api/v1/sjml/chains";
    String body = "{\"name\":\"x\"}";
    String secret = "test-secret";
    String expected = hmacSha256Base64(ts + nonce + method + path + body, secret);

    assertEquals(expected, ChainMpClient.sign(ts, nonce, method, path, body, secret));
  }

  /** 建链请求携带唯一 nonce，签名原文与实际发送体一致，并解析直接响应。 */
  @Test
  void createChain_sendsSignedRequestAndParsesRawResponse() throws Exception {
    AtomicReference<String> requestBody = new AtomicReference<>();
    AtomicReference<String> signature = new AtomicReference<>();
    AtomicReference<String> nonceHeader = new AtomicReference<>();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/api/v1/sjml/chains",
        exchange -> {
          requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
          signature.set(exchange.getRequestHeaders().getFirst("X-Signature"));
          nonceHeader.set(exchange.getRequestHeaders().getFirst("X-Nonce"));
          assertEquals("test-app", exchange.getRequestHeaders().getFirst("X-App-Id"));
          byte[] response =
              "{\"chain_id\":\"chain-1\",\"status\":\"pending\"}"
                  .getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, response.length);
          exchange.getResponseBody().write(response);
          exchange.close();
        });
    server.start();

    ChainMpProperties properties = properties();
    properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
    ChainMpClient client =
        new ChainMpClient(
            properties,
            new ObjectMapper(),
            HttpClient.newHttpClient(),
            Clock.fixed(Instant.ofEpochSecond(1721894400), ZoneOffset.UTC));

    SjmlChainSummary chain =
        client.createChain(new SjmlCreateChainRequest("测试链", null, null));

    assertEquals("chain-1", chain.chainId());
    assertEquals("pending", chain.status());
    assertNotNull(nonceHeader.get(), "签名请求必须发送 X-Nonce");
    assertEquals(32, nonceHeader.get().length(), "UUID nonce 应为 32 位十六进制字符串");
    assertEquals(
        ChainMpClient.sign(
            "1721894400",
            nonceHeader.get(),
            "POST",
            "/api/v1/sjml/chains",
            requestBody.get(),
            "test-secret"),
        signature.get());
  }

  /** 按 ChainMP 真实字段解析节点列表。 */
  @Test
  void listNodes_parsesRawArrayAndActualFieldNames() throws Exception {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/api/v1/sjml/chains/chain-real/nodes",
        exchange -> {
          byte[] response =
              ("[{\"node_id\":\"node-1\",\"sequence\":1,"
                      + "\"data\":{\"content_hash\":\"abc123\"},"
                      + "\"data_hash\":\"node-hash\",\"on_chain_status\":\"confirmed\","
                      + "\"tx_hash\":\"0xtx\",\"block_height\":1454,"
                      + "\"confirmed_at\":\"2026-08-17T08:25:43.785293\"}]")
                  .getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, response.length);
          exchange.getResponseBody().write(response);
          exchange.close();
        });
    server.start();

    ChainMpProperties properties = properties();
    properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
    ChainMpClient client =
        new ChainMpClient(
            properties,
            new ObjectMapper(),
            HttpClient.newHttpClient(),
            Clock.fixed(Instant.ofEpochSecond(1721894400), ZoneOffset.UTC));

    List<SjmlNodeSummary> nodes = client.listNodes("chain-real");

    assertEquals(1, nodes.size());
    assertEquals(1, nodes.get(0).sequenceNo());
    assertEquals("abc123", nodes.get(0).data().get("content_hash"));
    assertEquals(1454L, nodes.get(0).blockNumber());
    assertEquals("2026-08-17T08:25:43.785293", nodes.get(0).confirmedAt());
  }

  /** 公开接口不带签名头。 */
  @Test
  void publicChain_omitsSignatureHeaders() throws Exception {
    AtomicReference<String> signature = new AtomicReference<>();
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/api/v1/sjml/chains/",
        exchange -> {
          signature.set(exchange.getRequestHeaders().getFirst("X-Signature"));
          byte[] response =
              "{\"chain_id\":\"c1\",\"nodes\":[]}"
                  .getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, response.length);
          exchange.getResponseBody().write(response);
          exchange.close();
        });
    server.start();

    ChainMpProperties properties = properties();
    properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
    ChainMpClient client =
        new ChainMpClient(
            properties,
            new ObjectMapper(),
            HttpClient.newHttpClient(),
            Clock.fixed(Instant.ofEpochSecond(1721894400), ZoneOffset.UTC));

    SjmlPublicChainResponse pub = client.publicChain("c1");

    assertEquals("c1", pub.chainId());
    assertNull(signature.get(), "公开接口不应携带 X-Signature");
  }

  /** 用独立 HMAC 计算期望签名，验证 sign 的 material 拼接顺序。 */
  private static String hmacSha256Base64(String material, String secret) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return Base64.getEncoder().encodeToString(mac.doFinal(material.getBytes(StandardCharsets.UTF_8)));
  }

  /** 构造测试配置。 */
  private static ChainMpProperties properties() {
    ChainMpProperties properties = new ChainMpProperties();
    properties.setAppId("test-app");
    properties.setAppSecret("test-secret");
    return properties;
  }
}
