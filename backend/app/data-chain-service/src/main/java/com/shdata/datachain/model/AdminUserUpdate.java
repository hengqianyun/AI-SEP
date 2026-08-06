package com.shdata.datachain.model;

/**
 * 管理员更新用户；软删权威形状为 {@code deleted: true}。 password 仅作写输入，响应永不回显。
 */
public record AdminUserUpdate(
    String displayName, Role role, String enterpriseName, String password, Boolean deleted) {}
