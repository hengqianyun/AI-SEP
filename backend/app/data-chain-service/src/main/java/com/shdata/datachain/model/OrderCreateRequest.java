package com.shdata.datachain.model;

/**
 * 链内创建订单请求体，对齐 contracts/openapi/openapi.yaml OrderCreateRequest。
 *
 * <p>仅 USER 可调用；授权 scope 仅信 SessionPrincipal。
 *
 * @param productId 产品 ID
 * @param remark 备注（不可为空）
 * @param noticeAccepted 是否已阅读并同意平台统一须知（必须为 true）
 * @author developer-wsc-913
 */
public record OrderCreateRequest(
    String productId,
    String remark,
    Boolean noticeAccepted) {}
