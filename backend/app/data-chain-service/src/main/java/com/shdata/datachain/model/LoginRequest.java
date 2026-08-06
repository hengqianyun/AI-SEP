package com.shdata.datachain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 登录请求。角色不得由客户端选择；会话角色仅来自 sys_user（V1.4）。
 * 忽略未知字段（含已废止的 role），避免旧客户端误传导致 400。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginRequest(String username, String password) {}
