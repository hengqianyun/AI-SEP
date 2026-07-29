package com.wsc.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final DemoAccountService demoAccounts;
  private final AuthAuditLogger auditLogger;

  public AuthController(DemoAccountService demoAccounts, AuthAuditLogger auditLogger) {
    this.demoAccounts = demoAccounts;
    this.auditLogger = auditLogger;
  }

  @PostMapping("/session")
  public ResponseEntity<Map<String, Object>> login(
      @RequestBody LoginRequest body, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    String username = body != null ? body.username() : null;
    String password = body != null ? body.password() : null;
    Role roleOverride = null;
    try {
      if (body != null && body.role() != null && !body.role().isBlank()) {
        roleOverride = Role.fromString(body.role());
      }
    } catch (IllegalArgumentException ex) {
      auditLogger.loginFailed(username, "INVALID_ROLE", correlationId);
      return unauthorized(correlationId, "未登录或会话已失效");
    }

    Optional<SessionPrincipal> principal =
        demoAccounts.authenticate(username, password, roleOverride);
    if (principal.isEmpty()) {
      auditLogger.loginFailed(username, "BAD_CREDENTIALS", correlationId);
      return unauthorized(correlationId, "未登录或会话已失效");
    }

    // 登录前先失效旧会话，避免会话固定
    HttpSession old = request.getSession(false);
    if (old != null) {
      old.invalidate();
    }
    HttpSession session = request.getSession(true);
    session.setAttribute(SessionKeys.PRINCIPAL, principal.get());

    return ResponseEntity.ok(ApiEnvelope.ok(SessionViews.toMap(principal.get()), correlationId));
  }

  @GetMapping("/session")
  public ResponseEntity<Map<String, Object>> current(HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    SessionPrincipal principal = currentPrincipal(request);
    if (principal == null) {
      return unauthorized(correlationId, "未登录或会话已失效");
    }
    return ResponseEntity.ok(ApiEnvelope.ok(SessionViews.toMap(principal), correlationId));
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
   * 角色切换：立即失效旧会话并建立新会话。旧 sessionId 后续写 API 须拒绝。
   * 非 OpenAPI 冻结路径，属 §3.2「或等价」实现。
   */
  @PostMapping("/session/role")
  public ResponseEntity<Map<String, Object>> switchRole(
      @RequestBody SwitchRoleRequest body, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    SessionPrincipal current = currentPrincipal(request);
    if (current == null) {
      return unauthorized(correlationId, "未登录或会话已失效");
    }
    Role newRole;
    try {
      newRole = body != null ? Role.fromString(body.role()) : null;
    } catch (IllegalArgumentException ex) {
      return unauthorized(correlationId, "未登录或会话已失效");
    }
    Optional<SessionPrincipal> next = demoAccounts.switchRole(current, newRole);
    if (next.isEmpty()) {
      return unauthorized(correlationId, "未登录或会话已失效");
    }

    HttpSession old = request.getSession(false);
    if (old != null) {
      old.invalidate();
    }
    HttpSession session = request.getSession(true);
    session.setAttribute(SessionKeys.PRINCIPAL, next.get());
    return ResponseEntity.ok(ApiEnvelope.ok(SessionViews.toMap(next.get()), correlationId));
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
