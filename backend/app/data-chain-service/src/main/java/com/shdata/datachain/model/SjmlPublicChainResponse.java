package com.shdata.datachain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 公开溯源查询响应（GET /api/v1/sjml/chains/{chain_id}/public，无需 HMAC）。
 *
 * @param chainId 溯源链 ID
 * @param nodes   节点列表（脱敏）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SjmlPublicChainResponse(
    @JsonProperty("chain_id") String chainId,
    @JsonProperty("nodes") List<SjmlNodeSummary> nodes) {}
