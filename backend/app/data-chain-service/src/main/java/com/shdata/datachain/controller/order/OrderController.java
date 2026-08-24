package com.shdata.datachain.controller.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.common.response.ServiceResult;
import com.shdata.datachain.common.support.CorrelationIdSupport;
import com.shdata.datachain.controller.security.AuthController;
import com.shdata.datachain.model.ContractConfirmRequest;
import com.shdata.datachain.model.OrderCreateRequest;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.model.TransactionInfoRequest;
import com.shdata.datachain.service.order.OrderService;
import com.shdata.datachain.service.security.SessionAuthSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * 订单 Controller — 只做编排，业务逻辑下沉到 {@link OrderService}。
 *
 * <p>对齐 contracts/openapi/openapi.yaml 订单路径：
 * <ul>
 *   <li>{@code POST /orders} — 链内创建订单（仅 USER）</li>
 *   <li>{@code GET /orders} — 订单列表（按角色过滤 + 分页）</li>
 *   <li>{@code GET /orders/{orderId}} — 订单详情</li>
 *   <li>{@code POST /orders/{orderId}/confirm} — 确认订单</li>
 *   <li>{@code POST /orders/{orderId}/contract} — 提交合约附件与交易信息</li>
 *   <li>{@code POST /orders/{orderId}/contract/confirm} — 统一确认合约</li>
 *   <li>{@code POST /orders/{orderId}/cancel} — 取消订单</li>
 * </ul>
 *
 * <p>授权 scope 仅信 {@link SessionPrincipal}；客户端 enterpriseId/userId/demandUserId 参数不作为授权依据。
 *
 * @author developer-wsc-913 / developer-wsc-916
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public OrderController(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    /**
     * 链内创建订单（仅 USER；产品允许简易流程）。
     *
     * @param body 创建请求体
     * @param request 当前请求
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody OrderCreateRequest body,
            HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request, correlationId);
        if (denied.isPresent()) {
            return denied.get();
        }

        SessionPrincipal principal = AuthController.currentPrincipal(request);
        ServiceResult result = orderService.createOrder(principal.userId(), body);
        return result.toResponseEntity(correlationId);
    }

    /**
     * 平台统一须知正文。
     *
     * <p>返回当前生效的平台统一须知正文与版本标识。
     * notice_version 由服务端自动填充至 t_order_header，前端无需回传版本号。
     *
     * @return 须知正文与版本
     */
    @GetMapping("/notices/current")
    public ResponseEntity<Map<String, Object>> getCurrentNotice(HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Map<String, Object> notice = orderService.getCurrentNotice();
        return ServiceResult.ok(notice).toResponseEntity(correlationId);
    }

    /**
     * 订单列表（分页；按角色过滤）。
     *
     * @param keyword 模糊搜索
     * @param status 按状态筛选
     * @param page 页码
     * @param pageSize 页大小
     * @param request 当前请求
     * @return 分页列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request, correlationId);
        if (denied.isPresent()) {
            return denied.get();
        }

        SessionPrincipal principal = AuthController.currentPrincipal(request);
        ServiceResult result = orderService.listOrders(
                principal.userId(), principal.role(), keyword, status, page, pageSize);
        return result.toResponseEntity(correlationId);
    }

    /**
     * 订单详情。
     *
     * @param orderId 订单 ID（UUID v4 格式或数据库主键）
     * @param request 当前请求
     * @return 订单详情或 403/404
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(
            @PathVariable String orderId,
            HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request, correlationId);
        if (denied.isPresent()) {
            return denied.get();
        }

        SessionPrincipal principal = AuthController.currentPrincipal(request);
        ServiceResult result = orderService.getOrderDetail(
                orderId, principal.userId(), principal.role());
        return result.toResponseEntity(correlationId);
    }

    /**
     * 确认订单（PROVIDER 本企业 / ADMIN 代确认）。
     *
     * <p>纯动作端点，无 request body（空 JSON 对象）。
     * PROVIDER 本企业或 ADMIN；PENDING_CONFIRM → PENDING_UPLOAD。USER → 403。
     *
     * @param orderId 订单 ID
     * @param request 当前请求
     * @return 确认结果
     */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(
            @PathVariable String orderId,
            HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request, correlationId);
        if (denied.isPresent()) {
            return denied.get();
        }

        SessionPrincipal principal = AuthController.currentPrincipal(request);
        ServiceResult result = orderService.confirmOrder(
                orderId, principal.userId(), principal.role());
        return result.toResponseEntity(correlationId);
    }

    /**
     * 提交合约附件与交易信息（仅需求方本人）。
     *
     * <p>multipart 硬冻结：part file（签署附件）+ part transactionInfo（JSON string）。
     * PENDING_UPLOAD → PENDING_CONTRACT_CONFIRM。
     *
     * @param orderId 订单 ID
     * @param file 签署附件（Word/PDF，≤20MB）
     * @param transactionInfo JSON string（交易信息）
     * @param request 当前请求
     * @return 提交结果
     */
    @PostMapping("/{orderId}/contract")
    public ResponseEntity<Map<String, Object>> submitContract(
            @PathVariable String orderId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("transactionInfo") String transactionInfo,
            HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request, correlationId);
        if (denied.isPresent()) {
            return denied.get();
        }

        SessionPrincipal principal = AuthController.currentPrincipal(request);

        // 解析 transactionInfo JSON
        TransactionInfoRequest txInfo;
        try {
            txInfo = objectMapper.readValue(transactionInfo, TransactionInfoRequest.class);
        } catch (Exception e) {
            log.warn("Failed to parse transactionInfo JSON: {}", e.getMessage());
            return ServiceResult.fail(400, "ERR_ORDER_AMOUNT_INVALID",
                    "交易信息 JSON 格式非法", null).toResponseEntity(correlationId);
        }

        // 从 MultipartFile 提取元信息
        String fileName = file.getOriginalFilename();
        String fileType = file.getContentType();
        long fileSize = file.getSize();

        // 注意：附件存储逻辑（如上传至对象存储）由后续实现补充；
        // 本期 mock 模式下存储键使用文件名
        String storageKey = "mock://" + fileName;

        ServiceResult result = orderService.submitContract(
                orderId, principal.userId(),
                fileName, fileType, fileSize, storageKey, txInfo);
        return result.toResponseEntity(correlationId);
    }

    /**
     * 统一确认合约（PROVIDER 本企业 / ADMIN 代确认）。
     *
     * <p>body：noticeAccepted=true。
     * PROVIDER 本企业或 ADMIN；PENDING_CONTRACT_CONFIRM → CONTRACT_REACHED。USER → 403。
     *
     * @param orderId 订单 ID
     * @param body 确认请求体
     * @param request 当前请求
     * @return 确认结果
     */
    @PostMapping("/{orderId}/contract/confirm")
    public ResponseEntity<Map<String, Object>> confirmContract(
            @PathVariable String orderId,
            @RequestBody ContractConfirmRequest body,
            HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request, correlationId);
        if (denied.isPresent()) {
            return denied.get();
        }

        SessionPrincipal principal = AuthController.currentPrincipal(request);
        ServiceResult result = orderService.confirmContract(
                orderId, principal.userId(), principal.role(), body);
        return result.toResponseEntity(correlationId);
    }

    /**
     * 取消订单（三未完成态；三方均可）。
     *
     * <p>三未完成态（PENDING_CONFIRM / PENDING_UPLOAD / PENDING_CONTRACT_CONFIRM）可取消。
     * USER 本人 / PROVIDER 本企业 / ADMIN 可代取消。
     * 终态（CONTRACT_REACHED / CANCELLED）不可取消。
     *
     * @param orderId 订单 ID
     * @param request 当前请求
     * @return 取消结果
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable String orderId,
            HttpServletRequest request) {
        String correlationId = CorrelationIdSupport.resolve(request);
        Optional<ResponseEntity<Map<String, Object>>> denied = requireLogin(request, correlationId);
        if (denied.isPresent()) {
            return denied.get();
        }

        SessionPrincipal principal = AuthController.currentPrincipal(request);
        ServiceResult result = orderService.cancelOrder(
                orderId, principal.userId(), principal.role());
        return result.toResponseEntity(correlationId);
    }

    /**
     * 校验登录状态。
     *
     * @param request 当前请求
     * @param correlationId 追踪 ID
     * @return 未登录时返回 401 响应；已登录返回 empty
     */
    private static Optional<ResponseEntity<Map<String, Object>>> requireLogin(
            HttpServletRequest request, String correlationId) {
        return SessionAuthSupport.requireLogin(request, correlationId);
    }
}
