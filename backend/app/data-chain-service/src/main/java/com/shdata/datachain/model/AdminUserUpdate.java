package com.shdata.datachain.model;

/**
 * 管理员更新用户（契约 AdminUserUpdate / 计划字面名 AdminUserUpdateRequest）。
 * 软删权威形状为 {@code deleted: true}。password 仅作写输入，响应永不回显。
 * {@code enterpriseId} 优先于 {@code enterpriseName}；均缺省则不改归属。
 */
public record AdminUserUpdate(
    String displayName,
    Role role,
    String enterpriseId,
    String enterpriseName,
    String password,
    Boolean deleted) {}
