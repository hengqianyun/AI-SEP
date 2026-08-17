package com.shdata.datachain.model;

/**
 * 管理员创建用户请求（契约 AdminUserCreate / 计划字面名 AdminUserCreateRequest）。
 * {@code enterpriseId} 可选，缺省继承操作者企业。
 */
public record AdminUserCreate(
    String username,
    String password,
    Role role,
    String displayName,
    String enterpriseId,
    String enterpriseName) {}
