package com.shdata.datachain.overview;

import com.shdata.datachain.security.ApiEnvelope;
import com.shdata.datachain.security.AuthController;
import com.shdata.datachain.security.CorrelationIdSupport;
import com.shdata.datachain.security.SessionPrincipal;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 总览只读 API，对齐 OpenAPI {@code /overview/*}。需登录会话；任意已登录角色可读。
 */
@RestController
@RequestMapping("/api/v1/overview")
public class OverviewController {

  private final OverviewService overviewService;

  public OverviewController(OverviewService overviewService) {
    this.overviewService = overviewService;
  }

  @GetMapping("/metrics")
  public ResponseEntity<Map<String, Object>> metrics(HttpServletRequest request) {
    return withSession(request, () -> overviewService.metrics());
  }

  @GetMapping("/trend")
  public ResponseEntity<Map<String, Object>> trend(
      HttpServletRequest request, @RequestParam(defaultValue = "30") int days) {
    return withSession(request, () -> overviewService.trend(days));
  }

  @GetMapping("/top")
  public ResponseEntity<Map<String, Object>> top(
      HttpServletRequest request, @RequestParam(defaultValue = "10") int limit) {
    return withSession(request, () -> overviewService.top(limit));
  }

  @GetMapping("/stream")
  public ResponseEntity<Map<String, Object>> stream(
      HttpServletRequest request, @RequestParam(defaultValue = "10") int limit) {
    return withSession(request, () -> overviewService.stream(limit));
  }

  @GetMapping("/distribution")
  public ResponseEntity<Map<String, Object>> distribution(HttpServletRequest request) {
    return withSession(request, () -> overviewService.distribution());
  }

  private ResponseEntity<Map<String, Object>> withSession(
      HttpServletRequest request, java.util.function.Supplier<Map<String, Object>> supplier) {
    String correlationId = CorrelationIdSupport.resolve(request);
    SessionPrincipal principal = AuthController.currentPrincipal(request);
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiEnvelope.error("401", "未登录或会话已失效", null, correlationId));
    }
    return ResponseEntity.ok(ApiEnvelope.ok(supplier.get(), correlationId));
  }
}
