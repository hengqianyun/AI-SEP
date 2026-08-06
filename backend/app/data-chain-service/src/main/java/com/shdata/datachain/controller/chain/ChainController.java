package com.shdata.datachain.controller.chain;

import com.shdata.datachain.common.support.ApiEnvelope;
import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.service.security.SessionAuthSupport;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import com.shdata.datachain.service.chain.ChainService;
import org.springframework.http.ResponseEntity;
import com.shdata.datachain.service.chain.ChainService;
import org.springframework.web.bind.annotation.GetMapping;
import com.shdata.datachain.service.chain.ChainService;
import org.springframework.web.bind.annotation.PathVariable;
import com.shdata.datachain.service.chain.ChainService;
import org.springframework.web.bind.annotation.RequestMapping;
import com.shdata.datachain.service.chain.ChainService;
import org.springframework.web.bind.annotation.RestController;
import com.shdata.datachain.service.chain.ChainService;

/**
 * 上链只读 API — 对齐 OpenAPI {@code /chain/products/{productId}/versions} 与 {@code
 * /chain/versions/{versionId}/snapshot}。需登录。
 */
@RestController
@RequestMapping("/api/v1/chain")
public class ChainController {

    private final ChainService chainService;

    public ChainController(ChainService chainService) {
        this.chainService = chainService;
    }

    /**
     * 查询指定产品的上链版本列表（版本号降序）。
     *
     * @param productId 产品 ID
     * @param request   HTTP 请求（用于会话校验）
     * @return 版本列表
     */
    @GetMapping("/products/{productId}/versions")
    public ResponseEntity<Map<String, Object>> listVersions(
            @PathVariable String productId, HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Optional<ResponseEntity<Map<String, Object>>> denied =
                SessionAuthSupport.requireLogin(request, correlationId);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ResponseEntity.ok(
                ApiEnvelope.ok(chainService.listVersions(productId), correlationId));
    }

    /**
     * 查询指定版本的上链快照详情。
     *
     * @param versionId 版本 ID
     * @param request   HTTP 请求（用于会话校验）
     * @return 快照数据，不存在返回 404
     */
    @GetMapping("/versions/{versionId}/snapshot")
    public ResponseEntity<Map<String, Object>> getSnapshot(
            @PathVariable String versionId, HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Optional<ResponseEntity<Map<String, Object>>> denied =
                SessionAuthSupport.requireLogin(request, correlationId);
        if (denied.isPresent()) {
            return denied.get();
        }
        return chainService
                .getSnapshot(versionId)
                .map(
                        snapshot ->
                                ResponseEntity.ok(ApiEnvelope.ok(snapshot, correlationId)))
                .orElseGet(
                        () ->
                                ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(
                                                ApiEnvelope.error(
                                                        "404", "上链版本快照不存在", null,
                                                        correlationId)));
    }
}
