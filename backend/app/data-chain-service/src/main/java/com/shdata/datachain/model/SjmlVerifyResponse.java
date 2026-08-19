package com.shdata.datachain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 验证溯源链完整性响应（GET /api/v1/sjml/chains/{chain_id}/verify）。
 *
 * @param chainId 溯源链 ID
 * @param valid   是否完整一致
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SjmlVerifyResponse(
    @JsonProperty("chain_id") String chainId,
    @JsonProperty("is_valid") Boolean valid) {}
