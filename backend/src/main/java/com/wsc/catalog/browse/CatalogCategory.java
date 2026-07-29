package com.wsc.catalog.browse;

/** 行业分类（L1/L2）。 */
public record CatalogCategory(String id, String name, String level, String parentId) {}
