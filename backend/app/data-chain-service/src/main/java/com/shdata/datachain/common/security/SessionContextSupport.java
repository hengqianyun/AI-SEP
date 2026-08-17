package com.shdata.datachain.common.security;

import com.shdata.datachain.model.SessionPrincipal;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 会话主体序列化（REQ-USER-002）。enterpriseId + enterpriseName 为 scope 单一真源输出。
 */
public final class SessionContextSupport {

    private SessionContextSupport() {}

    /**
     * 将会话主体转为 OpenAPI {@code Session} Map（必含 enterpriseId / enterpriseName）。
     *
     * @param principal 已认证主体，不得为 null
     * @return 不含密码字段的会话视图
     */
    public static Map<String, Object> toMap(SessionPrincipal principal) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", principal.userId());
        data.put("displayName", principal.displayName());
        data.put("role", principal.role().name());
        data.put("enterpriseId", principal.enterpriseId() == null ? "" : principal.enterpriseId());
        data.put(
                "enterpriseName",
                principal.enterpriseName() == null ? "" : principal.enterpriseName());
        return data;
    }
}
