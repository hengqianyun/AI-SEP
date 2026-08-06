package com.shdata.datachain.model;

/**
 * 用户管理响应模型。禁止含 password / passwordHash。
 */
public record AdminUser(
    String userId,
    String username,
    String displayName,
    Role role,
    String enterpriseName,
    boolean deleted,
    String createdAt,
    String updatedAt) {}
