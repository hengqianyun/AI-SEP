package com.shdata.datachain.controller.security;

import com.shdata.datachain.common.constant.SessionKeys;
import com.shdata.datachain.common.security.AuthAuditLogger;
import com.shdata.datachain.common.support.ApiEnvelope;
import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.common.security.SessionContextSupport;
import com.shdata.datachain.model.LoginRequest;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.service.security.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final UserAccountService userAccounts;
  private final AuthAuditLogger auditLogger;

  public AuthController(UserAccountService userAccounts, AuthAuditLogger auditLogger) {
    this.userAccounts = userAccounts;
    this.auditLogger = auditLogger;
  }

  @PostMapping("/session")
  public ResponseEntity<Map<String, Object>> login(
      @RequestBody LoginRequest body, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    String username = body != null ? body.username() : null;
    String password = body != null ? body.password() : null;

    Optional<SessionPrincipal> principal = userAccounts.authenticate(username, password);
    if (principal.isEmpty()) {
      auditLogger.loginFailed(username, "BAD_CREDENTIALS", correlationId);
      return unauthorized(correlationId, "未登录或会话已失效");
    }

    HttpSession old = request.getSession(false);
    if (old != null) {
      old.invalidate();
    }
    HttpSession session = request.getSession(true);
    session.setAttribute(SessionKeys.PRINCIPAL, principal.get());

    return ResponseEntity.ok(
        ApiEnvelope.ok(SessionContextSupport.toMap(principal.get()), correlationId));
  }

  @GetMapping("/session")
  public ResponseEntity<Map<String, Object>> current(HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    SessionPrincipal principal = currentPrincipal(request);
    if (principal == null) {
      return unauthorized(correlationId, "未登录或会话已失效");
    }
    return ResponseEntity.ok(
        ApiEnvelope.ok(SessionContextSupport.toMap(principal), correlationId));
  }

  @DeleteMapping("/session")
  public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return ResponseEntity.ok(ApiEnvelope.ok(null, correlationId));
  }

  /**
   * 角色切换已下线（V1.4：角色绑定账号）。保留端点返回 410。
   */
  @PostMapping("/session/role")
  public ResponseEntity<Map<String, Object>> switchRole(HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    return ResponseEntity.status(HttpStatus.GONE)
        .body(ApiEnvelope.error("ERR_ROLE_SWITCH_DISABLED", "角色由账号绑定，禁止会话切换", null, correlationId));
  }

  public static SessionPrincipal currentPrincipal(HttpServletRequest request) {
    try {
      HttpSession session = request.getSession(false);
      if (session == null) {
        return null;
      }
      Object attr = session.getAttribute(SessionKeys.PRINCIPAL);
      return attr instanceof SessionPrincipal p ? p : null;
    } catch (IllegalStateException invalidated) {
      return null;
    }
  }

  private static ResponseEntity<Map<String, Object>> unauthorized(
      String correlationId, String message) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiEnvelope.error("401", message, null, correlationId));
  }
}
