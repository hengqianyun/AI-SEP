package com.shdata.datachain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * 记录溯源节点请求（POST /api/v1/sjml/chains/{chain_id}/nodes）。字段对齐 sjml 接口文档。
 *
 * @param eventType 事件类型（必填），数据产品版本场景用 product_create / product_update
 * @param eventTime 事件时间（必填，ISO-8601）
 * @param data      节点业务数据（产品版本快照内容）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SjmlAppendNodeRequest(
    @JsonProperty("event_type") String eventType,
    @JsonProperty("event_time") String eventTime,
    @JsonProperty("data") Map<String, Object> data) {}
