package com.shdata.datachain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * 记录溯源节点响应。文档示例只含 {@code node_id} 与 {@code on_chain_status}；
 * 其余链上字段按 sjml 习惯预声明，缺失时为 null，{@code ignoreUnknown=true} 兼容增项。
 *
 * @param nodeId           节点 ID
 * @param onChainStatus    节点上链状态（pending / confirmed）
 * @param sequenceNo       节点序号（产品内版本号语义）
 * @param dataHash         节点数据哈希（服务端生成；若不返回则由本地 SHA-256 兜底）
 * @param data             节点业务数据，业务原文哈希位于 content_hash
 * @param transactionHash  链上交易哈希
 * @param blockNumber      区块高度
 * @param createdAt        平台创建时间（ISO-8601）
 * @param confirmedAt      平台确认时间（ISO-8601）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SjmlAppendNodeResponse(
    @JsonProperty("node_id") String nodeId,
    @JsonProperty("on_chain_status") String onChainStatus,
    @JsonProperty("sequence") Integer sequenceNo,
    @JsonProperty("data_hash") String dataHash,
    @JsonProperty("data") Map<String, Object> data,
    @JsonProperty("tx_hash") String transactionHash,
    @JsonProperty("block_height") Long blockNumber,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("confirmed_at") String confirmedAt) {}
