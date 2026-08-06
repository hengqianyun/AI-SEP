package com.shdata.datachain.model;

/** 管理员创建用户请求。 */
public record AdminUserCreate(
    String username, String password, Role role, String displayName, String enterpriseName) {}
