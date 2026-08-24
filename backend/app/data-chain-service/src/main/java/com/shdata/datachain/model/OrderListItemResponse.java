package com.shdata.datachain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表项响应，对齐 contracts/openapi/openapi.yaml OrderListItem（九组字段）。
 *
 * <p>①订单号 ②产品信息 ③提供方 ④需求方 ⑤金额 ⑥状态 ⑦上链次数 ⑧下单时间 ⑨操作（由 FE 按角色渲染）
 *
 * @param orderId 订单号（UUID v4 格式）
 * @param productName 产品名称（下单快照）
 * @param productCode 产品编码（下单快照）
 * @param productType 产品类型（下单快照）
 * @param sourcePlatform 来源平台（下单快照）
 * @param providerEnterpriseName 提供方企业名称
 * @param demandUserName 需求方用户显示名
 * @param demandUserLoginName 需求方用户登录名
 * @param demandEnterpriseName 需求方所属企业名称
 * @param amountTaxInclusive 含税金额
 * @param amountTaxExclusive 未税金额
 * @param currency 币种
 * @param status 订单状态
 * @param statusLabel 状态页面文案
 * @param chainCount 上链次数
 * @param createTime 下单时间
 * @author developer-wsc-913
 */
public record OrderListItemResponse(
    String orderId,
    String productName,
    String productCode,
    String productType,
    String sourcePlatform,
    String providerEnterpriseName,
    String demandUserName,
    String demandUserLoginName,
    String demandEnterpriseName,
    BigDecimal amountTaxInclusive,
    BigDecimal amountTaxExclusive,
    String currency,
    OrderStatus status,
    String statusLabel,
    Integer chainCount,
    LocalDateTime createTime) {}
