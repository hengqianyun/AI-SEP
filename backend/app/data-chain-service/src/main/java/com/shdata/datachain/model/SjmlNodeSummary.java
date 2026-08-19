package com.shdata.datachain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * 溯源节点摘要（查询节点列表项 / 公开查询 nodes 项）。字段对齐 sjml 接口文档。
 *
 * @param nodeId        节点 ID
 * @param sequenceNo    节点序号
 * @param onChainStatus 节点上链状态（若返回）
 * @param dataHash      节点数据哈希（若返回）
 * @param data          节点业务数据
 * @param transactionHash 链上交易哈希
 * @param blockNumber   区块高度
 * @param confirmedAt   链上确认时间
 * @param createdAt     节点创建时间
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SjmlNodeSummary(
    @JsonProperty("node_id") String nodeId,
    @JsonProperty("sequence") Integer sequenceNo,
    @JsonProperty("on_chain_status") String onChainStatus,
    @JsonProperty("data_hash") String dataHash,
    @JsonProperty("data") Map<String, Object> data,
    @JsonProperty("tx_hash") String transactionHash,
    @JsonProperty("block_height") Long blockNumber,
    @JsonProperty("confirmed_at") String confirmedAt,
    @JsonProperty("created_at") String createdAt) {}
