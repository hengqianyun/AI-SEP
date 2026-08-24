package com.shdata.datachain.common.port;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 订单存证 mock 适配器 — 对齐 OrderChainAttestationPort 契约（ISSUE-SEC-004）。
 *
 * <p>返回模拟哈希/高度/节点，禁止读取真实链节点配置（URL/私钥/证书）。
 * mock 记录的字段为模拟值，不匹配真实链浏览器可验证格式。
 *
 * <p>适配失败时调用方不得阻断订单主流程。
 */
@Component
public class SimulatedOrderChainAttestationAdapter implements OrderChainAttestationPort {

  private static final Logger log = LoggerFactory.getLogger(SimulatedOrderChainAttestationAdapter.class);

  /** 模拟区块高度自增计数（进程级，重启归零属 mock 预期行为）。 */
  private final AtomicLong blockHeightCounter = new AtomicLong(100_000L);

  /** 模拟节点标识（固定占位符，禁止真实链节点 URL）。 */
  private static final String MOCK_CHAIN_NODE = "simulated-node-local";

  @Override
  public OrderAttestationResult attestOrder(OrderAttestationRequest request) {
    log.debug(
        "Mock order attestation: orderId={}, status={}, actorRole={}, actorUserId={}",
        request.orderId(),
        request.status(),
        request.actorRole(),
        request.actorUserId());

    // 生成模拟哈希（UUID hex，不含真实链格式）
    String chainHash = UUID.randomUUID().toString().replace("-", "");
    long blockHeight = blockHeightCounter.incrementAndGet();
    Instant timestamp = Instant.now();

    return new OrderAttestationResult(chainHash, blockHeight, MOCK_CHAIN_NODE, timestamp);
  }
}
