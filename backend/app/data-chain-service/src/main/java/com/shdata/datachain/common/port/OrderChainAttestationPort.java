package com.shdata.datachain.common.port;

import java.time.Instant;

/**
 * 订单存证适配端口 — 对齐 contracts/chain/OrderChainAttestationPort.md（PLAN-WSC-9.2 §3.1 / ISSUE-API-001）。
 *
 * <p>与现有 {@link ChainAttestationPort}（面向产品目录快照）并列共存。
 * 业务层仅依赖本接口；禁止直连真实链 SDK。mock 实现禁止读取/暴露真实链节点配置。
 *
 * <p>每次订单状态变更（创建/确认订单/提交合约/确认合约/取消）均调用本接口。
 * 适配失败时调用方不得阻断订单主流程。
 */
public interface OrderChainAttestationPort {

  /**
   * 将订单状态变更事件提交至存证适配层。
   *
   * @param request 订单存证请求
   * @return 存证结果（mock 模拟字段）
   */
  OrderAttestationResult attestOrder(OrderAttestationRequest request);

  /**
   * 订单存证请求。
   *
   * @param orderId 订单 ID（UUID v4 格式）
   * @param status 当前状态枚举值
   * @param actorRole 操作人角色（ADMIN/PROVIDER/USER）
   * @param actorUserId 操作人用户 ID
   * @param snapshotJson 订单快照摘要 JSON
   */
  record OrderAttestationRequest(
      String orderId,
      String status,
      String actorRole,
      String actorUserId,
      String snapshotJson) {}

  /**
   * 订单存证结果（mock 模拟值）。
   *
   * @param chainHash 模拟哈希值（UUID 或随机 hex，不含真实链格式）
   * @param blockHeight 模拟区块高度（自增计数）
   * @param chainNode 模拟节点标识（固定占位符，禁止真实链节点 URL）
   * @param timestamp 存证时间戳
   */
  record OrderAttestationResult(
      String chainHash,
      long blockHeight,
      String chainNode,
      Instant timestamp) {}
}
