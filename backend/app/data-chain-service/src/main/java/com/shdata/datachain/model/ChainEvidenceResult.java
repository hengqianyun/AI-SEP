package com.shdata.datachain.model;

import java.time.Instant;

/**
 * 提交并确认后的统一上链结果。
 *
 * @param evidenceId 存证/节点编号（兼容旧 evidenceId 语义）
 * @param contentHash SHA-256 哈希（不带算法前缀）
 * @param status 上链状态，真实结果为 confirmed，降级结果为 mocked
 * @param transactionHash 链上交易哈希或 mock 凭证
 * @param blockNumber 区块高度，mock 时为空
 * @param timestamp 链上时间或降级时间
 * @param mocked 是否为重试耗尽后的降级结果
 * @param chainId 溯源链 ID（同一产品的所有版本共享）
 */
public record ChainEvidenceResult(
    String evidenceId,
    String contentHash,
    String status,
    String transactionHash,
    Long blockNumber,
    Instant timestamp,
    boolean mocked,
    String chainId) {

  /** 兼容旧调用（不携带 chainId）。 */
  public ChainEvidenceResult(
      String evidenceId,
      String contentHash,
      String status,
      String transactionHash,
      Long blockNumber,
      Instant timestamp,
      boolean mocked) {
    this(evidenceId, contentHash, status, transactionHash, blockNumber, timestamp, mocked, null);
  }
}
