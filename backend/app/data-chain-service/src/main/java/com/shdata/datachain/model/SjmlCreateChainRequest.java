package com.shdata.datachain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 创建溯源链请求（POST /api/v1/sjml/chains）。字段对齐 sjml 接口文档。
 *
 * @param name        溯源链名称（必填）
 * @param description 溯源链说明
 * @param tags        标签列表
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SjmlCreateChainRequest(
    @JsonProperty("name") String name,
    @JsonProperty("description") String description,
    @JsonProperty("tags") List<String> tags) {}
