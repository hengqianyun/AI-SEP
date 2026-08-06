package com.shdata.datachain.service.security;

import com.shdata.datachain.common.support.ApiEnvelope;
import com.shdata.datachain.controller.security.AuthController;
import com.shdata.datachain.model.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * 会话认证辅助工具，统一各 Controller 中的「需登录」校验模式。
 */
public final class SessionAuthSupport {

    private SessionAuthSupport() {}

    /**
     * 校验当前请求是否已登录；未登录返回 401 响应，已登录返回 {@link Optional#empty()}。
     */
    public static Optional<ResponseEntity<Map<String, Object>>> requireLogin(
            HttpServletRequest request, String correlationId) {
        SessionPrincipal principal = AuthController.currentPrincipal(request);
        if (principal != null) {
            return Optional.empty();
        }
        return Optional.of(
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiEnvelope.error("401", "未登录或会话已失效", null, correlationId)));
    }

    /** 获取当前登录用户 ID，未登录返回 "-"。 */
    public static String actorId(HttpServletRequest request) {
        SessionPrincipal p = AuthController.currentPrincipal(request);
        return p == null ? "-" : p.userId();
    }
}
