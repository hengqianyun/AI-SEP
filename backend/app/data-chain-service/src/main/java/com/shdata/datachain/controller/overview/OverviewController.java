package com.shdata.datachain.controller.overview;

import com.shdata.datachain.common.support.ApiEnvelope;
import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.service.security.SessionAuthSupport;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import com.shdata.datachain.service.overview.OverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import com.shdata.datachain.service.overview.OverviewService;
import org.springframework.web.bind.annotation.RequestMapping;
import com.shdata.datachain.service.overview.OverviewService;
import org.springframework.web.bind.annotation.RequestParam;
import com.shdata.datachain.service.overview.OverviewService;
import org.springframework.web.bind.annotation.RestController;
import com.shdata.datachain.service.overview.OverviewService;

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

    /**
     * 总览指标卡片（资产总数、活跃企业、今日存证）。
     *
     * @param request HTTP 请求
     * @return 指标数据
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics(HttpServletRequest request) {
        return withSession(request, overviewService::metrics);
    }

    /**
     * N 天存证趋势（1-90 天，默认 30）。
     *
     * @param request HTTP 请求
     * @param days    天数
     * @return 趋势数据点
     */
    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> trend(
            HttpServletRequest request, @RequestParam(defaultValue = "30") int days) {
        return withSession(request, () -> overviewService.trend(days));
    }

    /**
     * TOP N 热门产品排行（1-10，默认 10）。
     *
     * @param request HTTP 请求
     * @param limit   返回条数
     * @return 排行列表
     */
    @GetMapping("/top")
    public ResponseEntity<Map<String, Object>> top(
            HttpServletRequest request, @RequestParam(defaultValue = "10") int limit) {
        return withSession(request, () -> overviewService.top(limit));
    }

    /**
     * 最近 N 条实时事件流（1-100，默认 10）。
     *
     * @param request HTTP 请求
     * @param limit   返回条数
     * @return 事件流列表
     */
    @GetMapping("/stream")
    public ResponseEntity<Map<String, Object>> stream(
            HttpServletRequest request, @RequestParam(defaultValue = "10") int limit) {
        return withSession(request, () -> overviewService.stream(limit));
    }

    /**
     * 行业/地域分布饼图数据。
     *
     * @param request HTTP 请求
     * @return 分布数据
     */
    @GetMapping("/distribution")
    public ResponseEntity<Map<String, Object>> distribution(HttpServletRequest request) {
        return withSession(request, overviewService::distribution);
    }

    /**
     * 统一会话校验 + 响应包装。
     */
    private ResponseEntity<Map<String, Object>> withSession(
            HttpServletRequest request, java.util.function.Supplier<Map<String, Object>> supplier) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Optional<ResponseEntity<Map<String, Object>>> denied =
                SessionAuthSupport.requireLogin(request, correlationId);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ResponseEntity.ok(ApiEnvelope.ok(supplier.get(), correlationId));
    }
}
