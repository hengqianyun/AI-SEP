package com.wsc.chain;

import com.wsc.security.ApiEnvelope;
import com.wsc.security.AuthController;
import com.wsc.security.CorrelationIdSupport;
import com.wsc.security.SessionPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 上链只读 API — 对齐 OpenAPI {@code /chain/products/{productId}/versions} 与 {@code
 * /chain/versions/{versionId}/snapshot}。需登录。
 */
@RestController
@RequestMapping("/api/v1/chain")
public class ChainController {

  private final InMemoryChainStore store;

  public ChainController(InMemoryChainStore store) {
    this.store = store;
  }

  @GetMapping("/products/{productId}/versions")
  public ResponseEntity<Map<String, Object>> listVersions(
      @PathVariable String productId, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    ResponseEntity<Map<String, Object>> unauth = requireLogin(request, correlationId);
    if (unauth != null) {
      return unauth;
    }

    List<Map<String, Object>> items =
        store.listByProductId(productId).stream().map(this::toVersionView).toList();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("items", items);
    return ResponseEntity.ok(ApiEnvelope.ok(data, correlationId));
  }

  @GetMapping("/versions/{versionId}/snapshot")
  public ResponseEntity<Map<String, Object>> getSnapshot(
      @PathVariable String versionId, HttpServletRequest request) {
    String correlationId = CorrelationIdSupport.resolve(request);
    ResponseEntity<Map<String, Object>> unauth = requireLogin(request, correlationId);
    if (unauth != null) {
      return unauth;
    }

    return store
        .findByVersionId(versionId)
        .map(
            v -> {
              Map<String, Object> body = new LinkedHashMap<>(v.snapshot());
              return ResponseEntity.ok(ApiEnvelope.ok(body, correlationId));
            })
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiEnvelope.error("404", "上链版本快照不存在", null, correlationId)));
  }

  private Map<String, Object> toVersionView(InMemoryChainStore.StoredVersion v) {
    Map<String, Object> cert = new LinkedHashMap<>();
    cert.put("owner", v.certOwner());
    cert.put("timestamp", v.timestamp().toString());

    Map<String, Object> item = new LinkedHashMap<>();
    item.put("versionId", v.versionId());
    item.put("versionNo", v.versionNo());
    item.put("timestamp", v.timestamp().toString());
    item.put("metadataHash", v.metadataHash());
    item.put("ownerDID", v.ownerDID());
    item.put("certificate", cert);
    return item;
  }

  private static ResponseEntity<Map<String, Object>> requireLogin(
      HttpServletRequest request, String correlationId) {
    SessionPrincipal principal = AuthController.currentPrincipal(request);
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiEnvelope.error("401", "未登录或会话已失效", null, correlationId));
    }
    return null;
  }
}
