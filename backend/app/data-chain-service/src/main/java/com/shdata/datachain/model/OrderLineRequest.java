package com.shdata.datachain.model;

import java.math.BigDecimal;

/**
 * 计费明细行请求体，对齐 contracts/openapi/openapi.yaml OrderLine。
 *
 * <p>仅在 contract 阶段提交。
 *
 * @param unit 计量单位（不可为空）
 * @param quantity 数量（必须 > 0）
 * @param unitPrice 单价（人民币两位小数，必须 > 0）
 * @param subtotal 小计（人民币两位小数）
 * @author developer-wsc-916
 */
public record OrderLineRequest(
    String unit,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal) {}
