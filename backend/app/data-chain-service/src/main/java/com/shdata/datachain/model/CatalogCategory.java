package com.shdata.datachain.model;

/** 行业分类（L1 空间 / L2 行业 / L3 子类）。 */
public record CatalogCategory(String id, String name, String level, String parentId) {}
