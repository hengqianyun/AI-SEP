package com.shdata.datachain.model;

/**
 * 统一合约确认请求体，对齐 contracts/openapi/openapi.yaml ContractConfirmRequest。
 *
 * <p>PROVIDER 本企业或 ADMIN 代确认；PENDING_CONTRACT_CONFIRM → CONTRACT_REACHED。
 *
 * @param noticeAccepted 是否已阅读并同意平台统一须知（必须为 true）
 * @author developer-wsc-916
 */
public record ContractConfirmRequest(Boolean noticeAccepted) {}
