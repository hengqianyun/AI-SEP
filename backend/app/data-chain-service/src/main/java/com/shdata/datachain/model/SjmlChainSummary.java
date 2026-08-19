package com.shdata.datachain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 溯源链摘要（建链响应 / 链列表项）。字段对齐 sjml 接口文档；多余字段忽略。
 *
 * @param chainId     溯源链 ID
 * @param status      链状态（pending / confirmed）
 * @param name        溯源链名称（列表可能返回）
 * @param description 溯源链说明（列表可能返回）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SjmlChainSummary(
    @JsonProperty("chain_id") String chainId,
    @JsonProperty("status") String status,
    @JsonProperty("name") String name,
    @JsonProperty("description") String description) {}
