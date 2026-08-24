package com.shdata.datachain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情响应，对齐 contracts/openapi/openapi.yaml OrderDetail。
 *
 * <p>包含快照、参与方、状态、附件元数据、交易信息、时间线、mock 上链记录。
 *
 * @param orderId 订单号
 * @param status 订单状态
 * @param statusLabel 状态页面文案
 * @param productSnapshot 产品快照
 * @param participant 参与方信息
 * @param remark 备注
 * @param noticeVersion 须知版本
 * @param transactionInfo 交易信息（contract 阶段写入，可空）
 * @param attachment 附件元数据（contract 阶段写入，可空）
 * @param timeline 时间线事件列表
 * @param chainLogs mock 上链记录列表
 * @param chainCount 上链次数
 * @param amountTaxInclusive 含税金额
 * @param amountTaxExclusive 未税金额
 * @param currency 币种
 * @param createTime 下单时间
 * @param updateTime 最后更新时间
 * @author developer-wsc-913 / developer-wsc-916
 */
public record OrderDetailResponse(
    String orderId,
    OrderStatus status,
    String statusLabel,
    OrderProductSnapshot productSnapshot,
    OrderParticipant participant,
    String remark,
    String noticeVersion,
    OrderTransactionInfo transactionInfo,
    OrderAttachmentMeta attachment,
    List<OrderTimelineEventItem> timeline,
    List<OrderChainLogItem> chainLogs,
    List<ContractVersionItem> contractVersions,
    Integer chainCount,
    BigDecimal amountTaxInclusive,
    BigDecimal amountTaxExclusive,
    String currency,
    LocalDateTime createTime,
    LocalDateTime updateTime) {

  /**
   * 产品快照（下单时冻结，不跟产品漂移）。
   *
   * @param productId 产品 ID
   * @param productName 产品名称
   * @param productCode 产品编码
   * @param productType 产品类型
   * @param sourcePlatform 来源平台
   * @param allowSimpleOrder 是否允许链内简易流程
   */
  public record OrderProductSnapshot(
      String productId,
      String productName,
      String productCode,
      String productType,
      String sourcePlatform,
      Boolean allowSimpleOrder) {}

  /**
   * 订单参与方信息。
   *
   * @param providerEnterpriseId 提供方企业 ID
   * @param providerEnterpriseName 提供方企业名称
   * @param demandUserId 需求方用户 ID
   * @param demandUserName 需求方用户显示名
   * @param demandUserLoginName 需求方用户登录名
   * @param demandEnterpriseId 需求方所属企业 ID
   * @param demandEnterpriseName 需求方所属企业名称
   */
  public record OrderParticipant(
      String providerEnterpriseId,
      String providerEnterpriseName,
      String demandUserId,
      String demandUserName,
      String demandUserLoginName,
      String demandEnterpriseId,
      String demandEnterpriseName) {}

  /**
   * 时间线事件项。
   *
   * @param eventType 事件类型
   * @param description 事件描述
   * @param operator 操作人标识
   * @param operatorRole 操作人角色
   * @param occurredAt 发生时间
   */
  public record OrderTimelineEventItem(
      String eventType,
      String description,
      String operator,
      String operatorRole,
      LocalDateTime occurredAt) {}

  /**
   * mock 上链记录项。
   *
   * @param chainHash 模拟哈希值
   * @param blockHeight 模拟区块高度
   * @param chainNode 模拟节点标识
   * @param timestamp 存证时间戳
   */
  public record OrderChainLogItem(
      String chainHash,
      String blockHeight,
      String chainNode,
      LocalDateTime timestamp) {}

  /**
   * 交易信息（含税未税本期同价且保留两字段）。
   *
   * @param orderLines 计费明细行列表
   * @param amountTaxInclusive 含税金额
   * @param amountTaxExclusive 未税金额
   * @param currency 币种
   */
  public record OrderTransactionInfo(
      List<OrderLineItem> orderLines,
      BigDecimal amountTaxInclusive,
      BigDecimal amountTaxExclusive,
      String currency) {}

  /**
   * 计费明细行。
   *
   * @param unit 计量单位
   * @param quantity 数量
   * @param unitPrice 单价
   * @param subtotal 小计
   */
  public record OrderLineItem(
      String unit,
      Integer quantity,
      BigDecimal unitPrice,
      BigDecimal subtotal) {}

  /**
   * 附件元数据。
   *
   * @param fileName 文件名
   * @param fileType 文件类型
   * @param fileSize 文件大小（字节）
   * @param scanResult 扫描结果
   */
  public record OrderAttachmentMeta(
      String fileName,
      String fileType,
      Long fileSize,
      String scanResult) {}

  /**
   * 数字合约版本项。
   *
   * @param versionNo 版本号（从 1 递增）
   * @param snapshotJson 合约快照 JSON
   * @param createdAt 创建时间
   */
  public record ContractVersionItem(
      Integer versionNo,
      String snapshotJson,
      LocalDateTime createdAt) {}
}
