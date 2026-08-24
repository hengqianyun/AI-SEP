package com.shdata.datachain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易信息请求体（JSON string part of multipart），对齐 contracts/openapi/openapi.yaml OrderTransactionInfo。
 *
 * <p>仅在 contract 阶段提交。含税未税本期同价且保留两字段。
 *
 * @param orderLines 计费明细行列表
 * @param amountTaxInclusive 含税金额（人民币两位小数）
 * @param amountTaxExclusive 未税金额（人民币两位小数）
 * @param currency 币种（本期固定 CNY）
 * @author developer-wsc-916
 */
public record TransactionInfoRequest(
    List<OrderLineRequest> orderLines,
    BigDecimal amountTaxInclusive,
    BigDecimal amountTaxExclusive,
    String currency) {}
