package com.shdata.datachain.controller.security;

import com.shdata.datachain.common.exception.BusinessException;
import com.shdata.datachain.common.security.RbacMatrix;
import com.shdata.datachain.common.support.ApiEnvelope;
import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.model.AdminUser;
import com.shdata.datachain.model.AdminUserCreate;
import com.shdata.datachain.model.AdminUserPage;
import com.shdata.datachain.model.AdminUserUpdate;
import com.shdata.datachain.model.Role;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.service.security.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理员用户管理 API（REQ-USER-001）。响应永不含 password / passwordHash。
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserAccountService users;

    public AdminUserController(UserAccountService users) {
        this.users = users;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(HttpServletRequest request) {
        ResponseEntity<Map<String, Object>> denied = requireAdmin(request);
        if (denied != null) {
            return denied;
        }
        String correlationId = CorrelationIdSupport.resolve(request);
        AdminUserPage page = new AdminUserPage(users.listUsers());
        return ResponseEntity.ok(ApiEnvelope.ok(toPageMap(page), correlationId));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody AdminUserCreate body, HttpServletRequest request) {
        ResponseEntity<Map<String, Object>> denied = requireAdmin(request);
        if (denied != null) {
            return denied;
        }
        String correlationId = CorrelationIdSupport.resolve(request);
        SessionPrincipal principal = AuthController.currentPrincipal(request);
        try {
            if (body == null) {
                throw new BusinessException(400, "ERR_VALIDATION", "请求体不能为空", null);
            }
            Role role = body.role();
            AdminUser created =
                    users.createUser(
                            body.username(),
                            body.password(),
                            body.displayName(),
                            role,
                            body.enterpriseName(),
                            principal.userId());
            return ResponseEntity.ok(ApiEnvelope.ok(toUserMap(created), correlationId));
        } catch (BusinessException ex) {
            return ResponseEntity.status(ex.httpStatus())
                    .body(ApiEnvelope.error(ex.code(), ex.getMessage(), ex.data(), correlationId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiEnvelope.error("ERR_VALIDATION", ex.getMessage(), null, correlationId));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String userId,
            @RequestBody AdminUserUpdate body,
            HttpServletRequest request) {
        ResponseEntity<Map<String, Object>> denied = requireAdmin(request);
        if (denied != null) {
            return denied;
        }
        String correlationId = CorrelationIdSupport.resolve(request);
        SessionPrincipal principal = AuthController.currentPrincipal(request);
        try {
            long id = Long.parseLong(userId);
            if (body == null) {
                throw new BusinessException(400, "ERR_VALIDATION", "请求体不能为空", null);
            }
            AdminUser updated =
                    users.updateUser(
                            id,
                            body.displayName(),
                            body.role(),
                            body.enterpriseName(),
                            body.password(),
                            body.deleted(),
                            principal.userId());
            return ResponseEntity.ok(ApiEnvelope.ok(toUserMap(updated), correlationId));
        } catch (NumberFormatException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiEnvelope.error("ERR_VALIDATION", "userId 无效", null, correlationId));
        } catch (BusinessException ex) {
            return ResponseEntity.status(ex.httpStatus())
                    .body(ApiEnvelope.error(ex.code(), ex.getMessage(), ex.data(), correlationId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiEnvelope.error("ERR_VALIDATION", ex.getMessage(), null, correlationId));
        }
    }

    private static ResponseEntity<Map<String, Object>> requireAdmin(HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        SessionPrincipal principal = AuthController.currentPrincipal(request);
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiEnvelope.error("401", "未登录或会话已失效", null, correlationId));
        }
        if (!RbacMatrix.canManageUsers(principal.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiEnvelope.error("ERR_FORBIDDEN", "当前角色无权管理用户", null, correlationId));
        }
        return null;
    }

    /** 显式序列化，杜绝 password / passwordHash 泄漏。 */
    private static Map<String, Object> toUserMap(AdminUser u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", u.userId());
        m.put("username", u.username());
        m.put("displayName", u.displayName());
        m.put("role", u.role() == null ? null : u.role().name());
        m.put("enterpriseName", u.enterpriseName());
        m.put("deleted", u.deleted());
        if (u.createdAt() != null) {
            m.put("createdAt", u.createdAt());
        }
        if (u.updatedAt() != null) {
            m.put("updatedAt", u.updatedAt());
        }
        return m;
    }

    private static Map<String, Object> toPageMap(AdminUserPage page) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(
                "items",
                page.items().stream().map(AdminUserController::toUserMap).toList());
        return data;
    }
}
