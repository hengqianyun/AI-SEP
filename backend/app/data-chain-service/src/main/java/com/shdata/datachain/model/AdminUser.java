package com.shdata.datachain.model;

/**
 * 用户管理响应模型（契约 AdminUser / 计划字面名 AdminUserResponse）。禁止含 password / passwordHash。
 */
public record AdminUser(
    String userId,
    String username,
    String displayName,
    Role role,
    String enterpriseId,
    String enterpriseName,
    boolean deleted,
    String createdAt,
    String updatedAt) {}
