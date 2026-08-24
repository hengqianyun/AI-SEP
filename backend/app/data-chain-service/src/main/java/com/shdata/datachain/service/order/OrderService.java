package com.shdata.datachain.service.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.common.port.OrderAttachmentScanPort;
import com.shdata.datachain.common.port.OrderChainAttestationPort;
import com.shdata.datachain.common.port.OrderChainAttestationPort.OrderAttestationRequest;
import com.shdata.datachain.entity.DataProductEntity;
import com.shdata.datachain.entity.EnterpriseEntity;
import com.shdata.datachain.entity.SysUserEntity;
import com.shdata.datachain.entity.TOrderAttachmentEntity;
import com.shdata.datachain.entity.TOrderChainLogEntity;
import com.shdata.datachain.entity.TOrderEventEntity;
import com.shdata.datachain.entity.TOrderHeaderEntity;
import com.shdata.datachain.entity.TOrderLineEntity;
import com.shdata.datachain.model.ContractConfirmRequest;
import com.shdata.datachain.model.OrderCreateRequest;
import com.shdata.datachain.model.OrderDetailResponse;
import com.shdata.datachain.model.OrderDetailResponse.OrderAttachmentMeta;
import com.shdata.datachain.model.OrderDetailResponse.OrderChainLogItem;
import com.shdata.datachain.model.OrderDetailResponse.OrderLineItem;
import com.shdata.datachain.model.OrderDetailResponse.OrderParticipant;
import com.shdata.datachain.model.OrderDetailResponse.ContractVersionItem;
import com.shdata.datachain.model.OrderDetailResponse.OrderProductSnapshot;
import com.shdata.datachain.model.OrderDetailResponse.OrderTimelineEventItem;
import com.shdata.datachain.model.OrderDetailResponse.OrderTransactionInfo;
import com.shdata.datachain.model.OrderListItemResponse;
import com.shdata.datachain.model.OrderLineRequest;
import com.shdata.datachain.model.OrderPageResponse;
import com.shdata.datachain.model.OrderStatus;
import com.shdata.datachain.model.Role;
import com.shdata.datachain.model.TransactionInfoRequest;
import com.shdata.datachain.repository.DataProductRepository;
import com.shdata.datachain.repository.EnterpriseRepository;
import com.shdata.datachain.repository.SysUserRepository;
import com.shdata.datachain.repository.TOrderAttachmentRepository;
import com.shdata.datachain.repository.TOrderChainLogRepository;
import com.shdata.datachain.repository.TOrderEventRepository;
import com.shdata.datachain.repository.TOrderHeaderRepository;
import com.shdata.datachain.repository.TOrderLineRepository;
import com.shdata.datachain.common.response.ServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

    /**
     * Tips 订单 Service 功能清单：
     * - createOrder：创建订单（链内简易流程）
     * - listOrders：订单列表（按角色过滤 + 分页）
     * - getOrderDetail：订单详情
     * - confirmOrder：确认订单（PENDING_CONFIRM → PENDING_UPLOAD）
     * - submitContract：提交合约附件与交易信息（PENDING_UPLOAD → PENDING_CONTRACT_CONFIRM）
     * - confirmContract：统一确认合约（PENDING_CONTRACT_CONFIRM → CONTRACT_REACHED）
     * - cancelOrder：取消订单（三未完成态 → CANCELLED）
     * - createContractVersion：生成数字合约版本（EntityManager 直写 t_order_contract_version）
     * - queryContractVersions：查询合约版本列表（EntityManager 原生查询）
     *
     * <p>状态机：PENDING_CONFIRM → PENDING_UPLOAD → PENDING_CONTRACT_CONFIRM → CONTRACT_REACHED
     * <br>取消旁路：三未完成态均可 → CANCELLED；终态不可取消。
     * <br>数字合约版本：下单出 v1，每次状态变更出新版本并保留历史。
     *
     * @author developer-wsc-913 / developer-wsc-916 / developer-wsc-918
     */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    /** 允许的 pageSize 档位。 */
    private static final List<Integer> ALLOWED_PAGE_SIZES = Arrays.asList(10, 20, 50, 100);

    /** 平台统一须知版本号（当前版本；服务端自动填充）。 */
    private static final String CURRENT_NOTICE_VERSION = "v1.0";

    // ── 时间线事件类型常量 ──

    /** 事件类型：订单已创建。 */
    private static final String EVENT_ORDER_CREATED = "ORDER_CREATED";
    /** 事件类型：订单已确认。 */
    private static final String EVENT_ORDER_CONFIRMED = "ORDER_CONFIRMED";
    /** 事件类型：合约已提交。 */
    private static final String EVENT_CONTRACT_SUBMITTED = "CONTRACT_SUBMITTED";
    /** 事件类型：合约已确认。 */
    private static final String EVENT_CONTRACT_CONFIRMED = "CONTRACT_CONFIRMED";
    /** 事件类型：订单已取消。 */
    private static final String EVENT_ORDER_CANCELLED = "ORDER_CANCELLED";

    // ── 附件白名单 ──

    /** 白名单文件后缀。 */
    private static final List<String> ALLOWED_FILE_EXTENSIONS = Arrays.asList(".doc", ".docx", ".pdf");
    /** 单文件大小上限（字节：20MB）。 */
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private final TOrderHeaderRepository orderHeaderRepo;
    private final TOrderEventRepository orderEventRepo;
    private final TOrderChainLogRepository orderChainLogRepo;
    private final TOrderLineRepository orderLineRepo;
    private final TOrderAttachmentRepository orderAttachmentRepo;
    private final DataProductRepository productRepo;
    private final SysUserRepository sysUserRepo;
    private final EnterpriseRepository enterpriseRepo;
    private final OrderChainAttestationPort chainPort;
    private final OrderAttachmentScanPort scanPort;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    public OrderService(
            TOrderHeaderRepository orderHeaderRepo,
            TOrderEventRepository orderEventRepo,
            TOrderChainLogRepository orderChainLogRepo,
            TOrderLineRepository orderLineRepo,
            TOrderAttachmentRepository orderAttachmentRepo,
            DataProductRepository productRepo,
            SysUserRepository sysUserRepo,
            EnterpriseRepository enterpriseRepo,
            OrderChainAttestationPort chainPort,
            OrderAttachmentScanPort scanPort,
            ObjectMapper objectMapper,
            PlatformTransactionManager transactionManager) {
        this.orderHeaderRepo = orderHeaderRepo;
        this.orderEventRepo = orderEventRepo;
        this.orderChainLogRepo = orderChainLogRepo;
        this.orderLineRepo = orderLineRepo;
        this.orderAttachmentRepo = orderAttachmentRepo;
        this.productRepo = productRepo;
        this.sysUserRepo = sysUserRepo;
        this.enterpriseRepo = enterpriseRepo;
        this.chainPort = chainPort;
        this.scanPort = scanPort;
        this.objectMapper = objectMapper;
        this.transactionManager = transactionManager;
    }

    // ================================================================
    // 0. 平台统一须知
    // ================================================================

    /**
     * 获取当前生效的平台统一须知正文与版本标识。
     *
     * @return 包含 content 和 version 的 Map
     */
    public Map<String, Object> getCurrentNotice() {
        Map<String, Object> notice = new java.util.LinkedHashMap<>();
        notice.put("content", "1. 本平台交易订单功能仅提供链内简易流程的撮合记录，不构成法律意义上的合同签署。\n"
                + "2. 下单前请确认数据产品信息、价格及交付方式无误；订单创建后需供应方确认方可生效。\n"
                + "3. 交易附件（合约文件）由需求方上传，供应方确认后视为合约达成。\n"
                + "4. 所有交易记录均会上链存证，确保数据不可篡改、可追溯。\n"
                + "5. 如有疑问，请联系平台客服或查阅帮助中心。");
        notice.put("version", CURRENT_NOTICE_VERSION);
        return notice;
    }

    // ================================================================
    // 1. 创建订单
    // ================================================================

    /**
     * 创建订单（链内简易流程）。
     *
     * <p>校验顺序：产品存在 → 产品允许简易流程 → 备注非空 → 须知已勾选 →
     * 冻结产品快照 → 创建订单头 → 写时间线事件 → mock 上链（非阻断）。
     *
     * @param userId 创建人用户 ID
     * @param request 创建请求
     * @return 成功返回订单列表项；失败返回 ServiceResult.fail
     */
    @Transactional
    public ServiceResult createOrder(String userId, OrderCreateRequest request) {
        // -- 校验 productId --
        if (request.productId() == null || request.productId().isBlank()) {
            return ServiceResult.fail(400, "ERR_ORDER_REMARK_EMPTY", "产品 ID 不能为空", null);
        }

        // -- 查找产品 --
        Long productId;
        try {
            productId = Long.parseLong(request.productId());
        } catch (NumberFormatException e) {
            return ServiceResult.fail(400, "ERR_ORDER_NOT_FOUND", "产品 ID 格式非法", null);
        }
        Optional<DataProductEntity> productOpt = productRepo.findById(productId);
        if (productOpt.isEmpty()) {
            return ServiceResult.fail(400, "ERR_ORDER_NOT_FOUND", "产品不存在", null);
        }
        DataProductEntity product = productOpt.get();

        // -- 校验产品允许简易流程 --
        Boolean allowSimpleOrder = checkAllowSimpleOrder(productId);
        if (allowSimpleOrder == null || !allowSimpleOrder) {
            return ServiceResult.fail(400, "ERR_ORDER_SIMPLE_ORDER_NOT_ALLOWED",
                    "该产品不允许链内简易流程下单", null);
        }

        // -- 校验备注非空 --
        if (request.remark() == null || request.remark().isBlank()) {
            return ServiceResult.fail(400, "ERR_ORDER_REMARK_EMPTY", "备注不能为空", null);
        }

        // -- 校验须知已勾选 --
        if (request.noticeAccepted() == null || !request.noticeAccepted()) {
            return ServiceResult.fail(400, "ERR_ORDER_NOTICE_NOT_ACCEPTED",
                    "请阅读并同意平台统一须知", null);
        }

        // -- 获取提供方企业信息 --
        SysUserEntity providerUser = findProductCreator(product.getCreateBy());
        String providerEnterpriseId = providerUser != null ? String.valueOf(providerUser.getEnterpriseId()) : "";
        String providerEnterpriseName = providerUser != null ? providerUser.getEnterpriseName() : "";

        // -- 获取需求方信息 --
        SysUserEntity demandUser = sysUserRepo.findById(Long.parseLong(userId)).orElse(null);
        String demandUserName = demandUser != null ? demandUser.getDisplayName() : "";
        String demandUserLoginName = demandUser != null ? demandUser.getUsername() : "";
        String demandEnterpriseId = demandUser != null ? String.valueOf(demandUser.getEnterpriseId()) : "";
        String demandEnterpriseName = demandUser != null ? demandUser.getEnterpriseName() : "";

        // -- 冻结产品快照 JSON --
        String productSnapshotJson = buildProductSnapshot(product, allowSimpleOrder);

        // -- 生成订单号（UUID v4） --
        String orderNo = UUID.randomUUID().toString();

        // -- 创建订单头 --
        TOrderHeaderEntity header = new TOrderHeaderEntity();
        header.setOrderNo(orderNo);
        header.setStatus(OrderStatus.PENDING_CONFIRM.name());
        header.setDemandUserId(userId);
        header.setProviderEnterpriseId(providerEnterpriseId);
        header.setProductId(productId);
        header.setProductCode(product.getProductCode());
        header.setProductSnapshot(productSnapshotJson);
        header.setRemark(request.remark());
        header.setNoticeVersion(CURRENT_NOTICE_VERSION);
        header.setAmountTaxInclusive(BigDecimal.ZERO);
        header.setAmountTaxExclusive(BigDecimal.ZERO);
        header.setCurrency("CNY");
        header.setChainCount(0);
        header.setCreateBy(userId);
        header.setUpdateBy(userId);
        header = orderHeaderRepo.save(header);

        // -- 写时间线事件（ORDER_CREATED） --
        writeTimelineEvent(header.getId(), EVENT_ORDER_CREATED, "订单已创建", userId, "USER");

        // -- mock 上链（非阻断） --
        performMockAttestation(header, product, orderNo, OrderStatus.PENDING_CONFIRM.name(), "USER", userId);

        // -- 生成数字合约初版（v1） --
        createContractVersion(header.getId(), 1, productSnapshotJson, userId);

        // -- 组装返回 --
        OrderListItemResponse response = buildListItemResponse(header, product, providerEnterpriseName,
                demandUserName, demandUserLoginName, demandEnterpriseName);
        return ServiceResult.ok(response);
    }

    // ================================================================
    // 2. 订单列表
    // ================================================================

    /**
     * 查询订单列表（按角色过滤 + 数据库层分页）。
     *
     * <p>角色范围：
     * <ul>
     *   <li>USER：仅本人需求方订单</li>
     *   <li>PROVIDER：仅本企业作为提供方的订单</li>
     *   <li>ADMIN：全部订单</li>
     * </ul>
     *
     * @param userId 当前用户 ID
     * @param role 当前角色
     * @param keyword 模糊搜索（订单号/产品名/编码/需求方姓名/单位）
     * @param status 状态筛选（可空）
     * @param page 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    /**
     * 查询订单列表（按角色过滤 + 数据库层分页）。
     *
     * <p>角色范围：
     * <ul>
     *   <li>USER：仅本人需求方订单</li>
     *   <li>PROVIDER：仅本企业作为提供方的订单</li>
     *   <li>ADMIN：全部订单</li>
     * </ul>
     *
     * <p>不使用 {@code @Transactional} 注解，而是手动管理事务：
     * 原生 SQL 路径使用 PROPAGATION_REQUIRED（只读），若原生 SQL 失败（H2 不支持 JSON_EXTRACT），
     * 则在 REQUIRES_NEW 独立事务中执行完整的 JPQL fallback + 列表组装，
     * 避免原生 SQL 异常污染外层事务导致 UnexpectedRollbackException。
     *
     * @param userId 当前用户 ID
     * @param role 当前角色
     * @param keyword 模糊搜索（订单号/产品名/编码/需求方姓名/单位）
     * @param status 状态筛选（可空）
     * @param page 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    public ServiceResult listOrders(String userId, Role role, String keyword,
                                     String status, int page, int pageSize) {
        // -- 校验 pageSize --
        if (!ALLOWED_PAGE_SIZES.contains(pageSize)) {
            return ServiceResult.fail(400, "ERR_ORDER_INVALID_STATE",
                    "pageSize 仅允许 10/20/50/100", null);
        }
        final int effectivePage = (page < 1) ? 1 : page;

        // -- 构建分页参数 --
        Pageable pageable = PageRequest.of(effectivePage - 1, pageSize);

        // -- 预处理筛选参数 --
        String statusParam = (status != null && !status.isBlank()) ? status.trim() : null;
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        // -- 按角色调用数据库层分页查询（原生 SQL + 异常时 REQUIRES_NEW 全量 fallback） --
        TransactionTemplate mainTx = new TransactionTemplate(transactionManager);
        mainTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        mainTx.setReadOnly(true);
        try {
            return mainTx.execute(txStatus -> {
                Page<TOrderHeaderEntity> resultPage = queryOrdersByRoleNative(userId, role, statusParam, kw, pageable);
                return assembleListResponse(resultPage, effectivePage, pageSize);
            });
        } catch (Exception e) {
            log.warn("Native SQL query failed (likely H2 in-memory DB), falling back to JPQL: {}",
                    e.getMessage());
            // 原生 SQL 失败时，Hibernate 已将当前事务标记为 rollback-only。
            // mainTx.execute() 捕获异常后会自动回滚该事务。
            // 在 REQUIRES_NEW 独立事务中执行完整的 JPQL fallback + 列表组装。
            TransactionTemplate newTx = new TransactionTemplate(transactionManager);
            newTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            newTx.setReadOnly(true);
            return newTx.execute(txStatus -> {
                Page<TOrderHeaderEntity> fallbackPage = queryOrdersByRoleFallback(userId, role, statusParam, pageable);
                return assembleListResponse(fallbackPage, effectivePage, pageSize);
            });
        }
    }

    // ================================================================
    // 3. 订单详情
    // ================================================================

    /**
     * 获取订单详情（含附件、交易信息、时间线、mock 上链记录）。
     *
     * @param orderId 订单 ID（数据库主键）或订单号
     * @param userId 当前用户 ID
     * @param role 当前角色
     * @return 订单详情或 403/404
     */
    @Transactional(readOnly = true)
    public ServiceResult getOrderDetail(String orderId, String userId, Role role) {
        // -- 查找订单 --
        TOrderHeaderEntity header = findOrderHeader(orderId);
        if (header == null) {
            return ServiceResult.fail(404, "ERR_ORDER_NOT_FOUND", "订单不存在", null);
        }

        // -- 角色校验 --
        if (!canAccessOrder(header, userId, role)) {
            return ServiceResult.fail(403, "ERR_ORDER_FORBIDDEN", "无权查看该订单", null);
        }

        // -- 组装详情 --
        OrderDetailResponse detail = toDetailResponse(header);
        return ServiceResult.ok(detail);
    }

    // ================================================================
    // 4. 确认订单（PENDING_CONFIRM → PENDING_UPLOAD）
    // ================================================================

    /**
     * 确认订单（PROVIDER 本企业 / ADMIN 代确认）。
     *
     * <p>状态必须为 PENDING_CONFIRM；变更后写时间线事件 + mock 上链。
     * ADMIN 代操作时 update_by 记录 ADMIN 用户标识。
     *
     * @param orderId 订单 ID 或订单号
     * @param userId 操作人用户 ID
     * @param role 操作人角色
     * @return 确认结果
     */
    @Transactional
    public ServiceResult confirmOrder(String orderId, String userId, Role role) {
        // -- 查找订单 --
        TOrderHeaderEntity header = findOrderHeader(orderId);
        if (header == null) {
            return ServiceResult.fail(404, "ERR_ORDER_NOT_FOUND", "订单不存在", null);
        }

        // -- 状态校验（不可回退：仅 PENDING_CONFIRM 可确认） --
        if (!OrderStatus.PENDING_CONFIRM.name().equals(header.getStatus())) {
            return ServiceResult.fail(400, "ERR_ORDER_INVALID_STATE",
                    "当前状态不允许确认订单", null);
        }

        // -- 角色范围校验 --
        if (!canConfirmOrCancelOrder(header, userId, role)) {
            return ServiceResult.fail(403, "ERR_ORDER_FORBIDDEN", "无权确认该订单", null);
        }

        // -- ADMIN 代操作时记录 ADMIN 用户标识 --
        String effectiveOperatorId = userId;
        String effectiveOperatorRole = role.name();
        if (role == Role.ADMIN) {
            // ADMIN 代操作：update_by = ADMIN 用户标识
            effectiveOperatorId = userId;
            effectiveOperatorRole = "ADMIN";
        }

        // -- 状态变更：PENDING_CONFIRM → PENDING_UPLOAD --
        header.setStatus(OrderStatus.PENDING_UPLOAD.name());
        header.setUpdateBy(effectiveOperatorId);
        orderHeaderRepo.save(header);

        // -- 写时间线事件 --
        writeTimelineEvent(header.getId(), EVENT_ORDER_CONFIRMED,
                role == Role.ADMIN ? "管理员代确认订单" : "订单已确认",
                effectiveOperatorId, effectiveOperatorRole);

        // -- mock 上链 --
        performMockAttestation(header, null, header.getOrderNo(),
                OrderStatus.PENDING_UPLOAD.name(), effectiveOperatorRole, effectiveOperatorId);

        // -- 生成新版本合约 --
        appendContractVersion(header.getId(), header, effectiveOperatorId);

        // -- 返回更新后的列表项 --
        OrderListItemResponse response = buildListItemResponseFromHeader(header);
        return ServiceResult.ok(response);
    }

    // ================================================================
    // 5. 提交合约附件与交易信息（PENDING_UPLOAD → PENDING_CONTRACT_CONFIRM）
    // ================================================================

    /**
     * 提交合约附件与交易信息（仅需求方本人）。
     *
     * <p>校验顺序：状态=PENDING_UPLOAD → 附件白名单+大小+扫描 → 交易信息校验 →
     * 保存附件+明细 → 更新金额 → 状态变更 → 写时间线+mock 上链。
     *
     * @param orderId 订单 ID 或订单号
     * @param userId 操作人用户 ID（需求方本人）
     * @param fileName 文件名
     * @param fileType 文件类型
     * @param fileSize 文件大小
     * @param storageKey 存储键
     * @param transactionInfo 交易信息
     * @return 提交结果
     */
    @Transactional
    public ServiceResult submitContract(String orderId, String userId,
                                         String fileName, String fileType, long fileSize, String storageKey,
                                         TransactionInfoRequest transactionInfo) {
        // -- 查找订单 --
        TOrderHeaderEntity header = findOrderHeader(orderId);
        if (header == null) {
            return ServiceResult.fail(404, "ERR_ORDER_NOT_FOUND", "订单不存在", null);
        }

        // -- 状态校验：仅 PENDING_UPLOAD 可提交合约 --
        if (!OrderStatus.PENDING_UPLOAD.name().equals(header.getStatus())) {
            return ServiceResult.fail(400, "ERR_ORDER_INVALID_STATE",
                    "当前状态不允许提交合约", null);
        }

        // -- 需求方本人校验 --
        if (!userId.equals(header.getDemandUserId())) {
            return ServiceResult.fail(403, "ERR_ORDER_FORBIDDEN", "仅需求方本人可提交合约", null);
        }

        // -- 附件校验 --
        // 1. 文件名非空
        if (fileName == null || fileName.isBlank()) {
            return ServiceResult.fail(400, "ERR_ORDER_ATTACHMENT_INVALID", "附件不能为空", null);
        }

        // 2. 白名单检查（Word .doc/.docx、PDF .pdf）
        String lowerFileName = fileName.toLowerCase();
        boolean isAllowedType = ALLOWED_FILE_EXTENSIONS.stream().anyMatch(lowerFileName::endsWith);
        if (!isAllowedType) {
            return ServiceResult.fail(400, "ERR_ORDER_ATTACHMENT_INVALID",
                    "附件格式仅支持 Word (.doc/.docx) 和 PDF (.pdf)", null);
        }

        // 3. 单文件 20MB 限制
        if (fileSize > MAX_FILE_SIZE) {
            return ServiceResult.fail(400, "ERR_ORDER_ATTACHMENT_TOO_LARGE",
                    "附件大小超过 20MB 限制", null);
        }

        // 4. 扫描端口校验
        OrderAttachmentScanPort.ScanResult scanResult = scanPort.scan(
                new OrderAttachmentScanPort.ScanRequest(fileName, fileType, fileSize, null));
        if (!scanResult.passed()) {
            return ServiceResult.fail(400, "ERR_ORDER_ATTACHMENT_SCAN_FAILED",
                    "附件扫描未通过: " + scanResult.message(), null);
        }

        // -- 交易信息校验 --
        ServiceResult txValidation = validateTransactionInfo(transactionInfo);
        if (txValidation != null) {
            return txValidation;
        }

        // -- 保存附件 --
        TOrderAttachmentEntity attachment = new TOrderAttachmentEntity();
        attachment.setOrderHeaderId(header.getId());
        attachment.setFileName(fileName);
        attachment.setFileType(fileType);
        attachment.setFileSize(fileSize);
        attachment.setStorageKey(storageKey);
        attachment.setScanResult(scanResult.scanResultCode());
        attachment.setCreateBy(userId);
        attachment.setUpdateBy(userId);
        orderAttachmentRepo.save(attachment);

        // -- 保存计费明细 --
        List<TOrderLineEntity> lines = new ArrayList<>();
        if (transactionInfo.orderLines() != null) {
            for (OrderLineRequest lineReq : transactionInfo.orderLines()) {
                TOrderLineEntity line = new TOrderLineEntity();
                line.setOrderHeaderId(header.getId());
                line.setUnit(lineReq.unit());
                line.setQuantity(lineReq.quantity());
                line.setUnitPrice(lineReq.unitPrice());
                line.setSubtotal(lineReq.subtotal());
                line.setCreateBy(userId);
                line.setUpdateBy(userId);
                lines.add(orderLineRepo.save(line));
            }
        }

        // -- 更新订单金额 --
        header.setAmountTaxInclusive(transactionInfo.amountTaxInclusive());
        header.setAmountTaxExclusive(transactionInfo.amountTaxExclusive());
        header.setCurrency(transactionInfo.currency() != null ? transactionInfo.currency() : "CNY");

        // -- 状态变更：PENDING_UPLOAD → PENDING_CONTRACT_CONFIRM --
        header.setStatus(OrderStatus.PENDING_CONTRACT_CONFIRM.name());
        header.setUpdateBy(userId);
        orderHeaderRepo.save(header);

        // -- 写时间线事件 --
        writeTimelineEvent(header.getId(), EVENT_CONTRACT_SUBMITTED, "合约附件与交易信息已提交",
                userId, "USER");

        // -- mock 上链 --
        performMockAttestation(header, null, header.getOrderNo(),
                OrderStatus.PENDING_CONTRACT_CONFIRM.name(), "USER", userId);

        // -- 生成新版本合约 --
        appendContractVersion(header.getId(), header, userId);

        // -- 返回更新后的列表项 --
        OrderListItemResponse response = buildListItemResponseFromHeader(header);
        return ServiceResult.ok(response);
    }

    // ================================================================
    // 6. 统一确认合约（PENDING_CONTRACT_CONFIRM → CONTRACT_REACHED）
    // ================================================================

    /**
     * 统一确认合约（PROVIDER 本企业 / ADMIN 代确认）。
     *
     * <p>body：noticeAccepted=true。
     * 状态必须为 PENDING_CONTRACT_CONFIRM。
     *
     * @param orderId 订单 ID 或订单号
     * @param userId 操作人用户 ID
     * @param role 操作人角色
     * @param request 确认请求体
     * @return 确认结果
     */
    @Transactional
    public ServiceResult confirmContract(String orderId, String userId, Role role,
                                          ContractConfirmRequest request) {
        // -- 查找订单 --
        TOrderHeaderEntity header = findOrderHeader(orderId);
        if (header == null) {
            return ServiceResult.fail(404, "ERR_ORDER_NOT_FOUND", "订单不存在", null);
        }

        // -- 状态校验：仅 PENDING_CONTRACT_CONFIRM 可确认合约 --
        if (!OrderStatus.PENDING_CONTRACT_CONFIRM.name().equals(header.getStatus())) {
            return ServiceResult.fail(400, "ERR_ORDER_INVALID_STATE",
                    "当前状态不允许确认合约", null);
        }

        // -- 须知校验 --
        if (request.noticeAccepted() == null || !request.noticeAccepted()) {
            return ServiceResult.fail(400, "ERR_ORDER_NOTICE_NOT_ACCEPTED",
                    "请阅读并同意平台统一须知", null);
        }

        // -- 角色范围校验 --
        if (!canConfirmOrCancelOrder(header, userId, role)) {
            return ServiceResult.fail(403, "ERR_ORDER_FORBIDDEN", "无权确认该合约", null);
        }

        // -- ADMIN 代操作时记录 ADMIN 用户标识 --
        String effectiveOperatorId = userId;
        String effectiveOperatorRole = role.name();

        // -- 状态变更：PENDING_CONTRACT_CONFIRM → CONTRACT_REACHED --
        header.setStatus(OrderStatus.CONTRACT_REACHED.name());
        header.setUpdateBy(effectiveOperatorId);
        orderHeaderRepo.save(header);

        // -- 写时间线事件 --
        writeTimelineEvent(header.getId(), EVENT_CONTRACT_CONFIRMED,
                role == Role.ADMIN ? "管理员代确认合约" : "合约已确认",
                effectiveOperatorId, effectiveOperatorRole);

        // -- mock 上链 --
        performMockAttestation(header, null, header.getOrderNo(),
                OrderStatus.CONTRACT_REACHED.name(), effectiveOperatorRole, effectiveOperatorId);

        // -- 生成新版本合约 --
        appendContractVersion(header.getId(), header, effectiveOperatorId);

        // -- 返回更新后的列表项 --
        OrderListItemResponse response = buildListItemResponseFromHeader(header);
        return ServiceResult.ok(response);
    }

    // ================================================================
    // 7. 取消订单（三未完成态 → CANCELLED）
    // ================================================================

    /**
     * 取消订单（USER 本人 / PROVIDER 本企业 / ADMIN 任意）。
     *
     * <p>三未完成态（PENDING_CONFIRM / PENDING_UPLOAD / PENDING_CONTRACT_CONFIRM）可取消。
     * 终态（CONTRACT_REACHED / CANCELLED）不可取消。
     *
     * @param orderId 订单 ID 或订单号
     * @param userId 操作人用户 ID
     * @param role 操作人角色
     * @return 取消结果
     */
    @Transactional
    public ServiceResult cancelOrder(String orderId, String userId, Role role) {
        // -- 查找订单 --
        TOrderHeaderEntity header = findOrderHeader(orderId);
        if (header == null) {
            return ServiceResult.fail(404, "ERR_ORDER_NOT_FOUND", "订单不存在", null);
        }

        // -- 状态校验：三未完成态可取消 --
        OrderStatus currentStatus = OrderStatus.valueOf(header.getStatus());
        if (!isCancellableState(currentStatus)) {
            return ServiceResult.fail(400, "ERR_ORDER_INVALID_STATE",
                    "当前状态不允许取消", null);
        }

        // -- 角色范围校验 --
        if (!canConfirmOrCancelOrder(header, userId, role)) {
            return ServiceResult.fail(403, "ERR_ORDER_FORBIDDEN", "无权取消该订单", null);
        }

        // -- ADMIN 代操作时记录 ADMIN 用户标识 --
        String effectiveOperatorId = userId;
        String effectiveOperatorRole = role.name();

        // -- 状态变更 → CANCELLED --
        header.setStatus(OrderStatus.CANCELLED.name());
        header.setUpdateBy(effectiveOperatorId);
        orderHeaderRepo.save(header);

        // -- 写时间线事件 --
        writeTimelineEvent(header.getId(), EVENT_ORDER_CANCELLED,
                role == Role.ADMIN ? "管理员代取消订单" : "订单已取消",
                effectiveOperatorId, effectiveOperatorRole);

        // -- mock 上链 --
        performMockAttestation(header, null, header.getOrderNo(),
                OrderStatus.CANCELLED.name(), effectiveOperatorRole, effectiveOperatorId);

        // -- 生成新版本合约 --
        appendContractVersion(header.getId(), header, effectiveOperatorId);

        // -- 返回更新后的列表项 --
        OrderListItemResponse response = buildListItemResponseFromHeader(header);
        return ServiceResult.ok(response);
    }

    // ================================================================
    // 私有辅助方法
    // ================================================================

    /**
     * 校验交易信息。
     *
     * <p>校验规则：
     * <ul>
     *   <li>明细行：单位不可为空、数量 > 0、单价 > 0</li>
     *   <li>小计 = 单价 × 数量（两位小数）</li>
     *   <li>含税 = 未税（本期同价）</li>
     *   <li>币种 = CNY</li>
     * </ul>
     *
     * @param transactionInfo 交易信息
     * @return 校验失败返回 ServiceResult.fail；校验通过返回 null
     */
    private ServiceResult validateTransactionInfo(TransactionInfoRequest transactionInfo) {
        if (transactionInfo == null) {
            return ServiceResult.fail(400, "ERR_ORDER_AMOUNT_INVALID", "交易信息不能为空", null);
        }

        // -- 含税 = 未税（本期同价） --
        if (transactionInfo.amountTaxInclusive() == null || transactionInfo.amountTaxExclusive() == null) {
            return ServiceResult.fail(400, "ERR_ORDER_AMOUNT_INVALID", "含税/未税金额不能为空", null);
        }
        if (transactionInfo.amountTaxInclusive().setScale(2, RoundingMode.HALF_UP)
                .compareTo(transactionInfo.amountTaxExclusive().setScale(2, RoundingMode.HALF_UP)) != 0) {
            return ServiceResult.fail(400, "ERR_ORDER_AMOUNT_INVALID",
                    "含税金额与未税金额必须相等（本期同价）", null);
        }

        // -- 币种 = CNY --
        if (!"CNY".equals(transactionInfo.currency())) {
            return ServiceResult.fail(400, "ERR_ORDER_AMOUNT_INVALID", "币种必须为 CNY", null);
        }

        // -- 明细行校验 --
        if (transactionInfo.orderLines() != null) {
            BigDecimal totalSubtotal = BigDecimal.ZERO;
            for (OrderLineRequest line : transactionInfo.orderLines()) {
                // 单位不可为空
                if (line.unit() == null || line.unit().isBlank()) {
                    return ServiceResult.fail(400, "ERR_ORDER_LINE_INVALID",
                            "明细行单位不能为空", null);
                }
                // 数量 > 0
                if (line.quantity() == null || line.quantity() <= 0) {
                    return ServiceResult.fail(400, "ERR_ORDER_LINE_INVALID",
                            "明细行数量必须大于 0", null);
                }
                // 单价 > 0
                if (line.unitPrice() == null || line.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    return ServiceResult.fail(400, "ERR_ORDER_LINE_INVALID",
                            "明细行单价必须大于 0", null);
                }
                // 小计 = 单价 × 数量（两位小数）
                BigDecimal expectedSubtotal = line.unitPrice()
                        .multiply(BigDecimal.valueOf(line.quantity()))
                        .setScale(2, RoundingMode.HALF_UP);
                if (line.subtotal() == null || line.subtotal().setScale(2, RoundingMode.HALF_UP)
                        .compareTo(expectedSubtotal) != 0) {
                    return ServiceResult.fail(400, "ERR_ORDER_LINE_INVALID",
                            "明细行小计不正确，应为 " + expectedSubtotal, null);
                }
                totalSubtotal = totalSubtotal.add(line.subtotal());
            }

            // 汇总金额与含税金额一致性校验
            if (totalSubtotal.setScale(2, RoundingMode.HALF_UP)
                    .compareTo(transactionInfo.amountTaxInclusive().setScale(2, RoundingMode.HALF_UP)) != 0) {
                return ServiceResult.fail(400, "ERR_ORDER_AMOUNT_INVALID",
                        "明细行小计之和与含税金额不一致", null);
            }
        }

        return null;
    }

    /**
     * 校验产品是否允许链内简易流程（原生查询 allow_simple_order 字段）。
     *
     * @param productId 产品 ID
     * @return true/false/null（字段不存在时）
     */
    private Boolean checkAllowSimpleOrder(Long productId) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT allow_simple_order FROM t_data_product WHERE id = :id AND del_flag = 0")
                    .setParameter("id", productId)
                    .getResultList();
            if (!results.isEmpty()) {
                Object val = results.get(0);
                if (val instanceof Number) {
                    return ((Number) val).intValue() == 1;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to query allow_simple_order for productId={}: {}", productId, e.getMessage());
        }
        return null;
    }

    /**
     * 查找产品创建者（提供方企业来源）。
     *
     * @param createBy 创建者用户 ID 字符串
     * @return 用户实体或 null
     */
    private SysUserEntity findProductCreator(String createBy) {
        if (createBy == null || createBy.isBlank()) {
            return null;
        }
        try {
            return sysUserRepo.findById(Long.parseLong(createBy)).orElse(null);
        } catch (NumberFormatException e) {
            return sysUserRepo.findByUsernameAndDelFlag(createBy, false).orElse(null);
        }
    }

    /**
     * 构建产品快照 JSON（冻结在下单时刻）。
     *
     * @param product 产品实体
     * @param allowSimpleOrder 是否允许简易流程
     * @return JSON 字符串
     */
    private String buildProductSnapshot(DataProductEntity product, Boolean allowSimpleOrder) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("productId", String.valueOf(product.getId()));
        snapshot.put("productName", product.getProductName());
        snapshot.put("productCode", product.getProductCode());
        snapshot.put("productType", product.getProductType());
        snapshot.put("sourcePlatform", "");
        snapshot.put("allowSimpleOrder", allowSimpleOrder);
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize product snapshot: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 构建上链快照摘要 JSON。
     *
     * @param header 订单头
     * @param product 产品实体（可空，为空时从快照取）
     * @return JSON 字符串
     */
    private String buildChainSnapshotJson(TOrderHeaderEntity header, DataProductEntity product) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("orderId", header.getOrderNo());
        snapshot.put("status", header.getStatus());
        snapshot.put("productCode", header.getProductCode());
        snapshot.put("demandUserId", header.getDemandUserId());
        snapshot.put("providerEnterpriseId", header.getProviderEnterpriseId());
        snapshot.put("createTime", header.getCreateTime() != null
                ? header.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
        if (product != null) {
            snapshot.put("productName", product.getProductName());
        }
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize chain snapshot: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 写时间线事件。
     *
     * @param orderHeaderId 订单头 ID
     * @param eventType 事件类型
     * @param description 事件描述
     * @param operatorId 操作人用户 ID
     * @param operatorRole 操作人角色
     */
    private void writeTimelineEvent(Long orderHeaderId, String eventType, String description,
                                     String operatorId, String operatorRole) {
        TOrderEventEntity event = new TOrderEventEntity();
        event.setOrderHeaderId(orderHeaderId);
        event.setEventType(eventType);
        event.setDescription(description);
        event.setOperatorId(operatorId);
        event.setOperatorRole(operatorRole);
        event.setCreateBy(operatorId);
        event.setUpdateBy(operatorId);
        orderEventRepo.save(event);
    }

    /**
     * 执行 mock 上链（非阻断）。
     *
     * <p>适配失败时不得阻断订单主流程（ISSUE-SEC-004 / acceptance）。
     *
     * @param header 订单头
     * @param product 产品实体（可空）
     * @param orderNo 订单号
     * @param status 当前状态
     * @param actorRole 操作人角色
     * @param actorUserId 操作人用户 ID
     */
    private void performMockAttestation(TOrderHeaderEntity header, DataProductEntity product,
                                         String orderNo, String status, String actorRole, String actorUserId) {
        String snapshotForChain = buildChainSnapshotJson(header, product);
        OrderAttestationRequest chainRequest = new OrderAttestationRequest(
                orderNo, status, actorRole, actorUserId, snapshotForChain);
        try {
            OrderChainAttestationPort.OrderAttestationResult chainResult =
                    chainPort.attestOrder(chainRequest);

            TOrderChainLogEntity chainLog = new TOrderChainLogEntity();
            chainLog.setOrderHeaderId(header.getId());
            chainLog.setEventType(status);
            chainLog.setChainHash(chainResult.chainHash());
            chainLog.setBlockHeight(String.valueOf(chainResult.blockHeight()));
            chainLog.setChainNode(chainResult.chainNode());
            chainLog.setChainTimestamp(chainResult.timestamp() != null
                    ? LocalDateTime.ofInstant(chainResult.timestamp(), java.time.ZoneId.systemDefault())
                    : LocalDateTime.now());
            chainLog.setCreateBy(actorUserId);
            chainLog.setUpdateBy(actorUserId);

            // 使用 REQUIRES_NEW 独立事务保存链日志，避免 IDENTITY 策略的即时 INSERT
            // 失败时污染主事务的 EntityManager（导致事务被标记为 rollback-only）。
            TransactionTemplate isolatedTx = new TransactionTemplate(transactionManager);
            isolatedTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            isolatedTx.executeWithoutResult(txStatus -> orderChainLogRepo.save(chainLog));

            // 更新上链次数（仅修改托管实体字段，由主事务在 commit 时 flush）。
            header.setChainCount(header.getChainCount() + 1);
        } catch (Exception e) {
            log.warn("Mock order attestation failed (non-blocking): orderId={}, error={}",
                    orderNo, e.getMessage());
        }
    }

    // ================================================================
    // 数字合约版本（REQ-WSC-ORDER-017）
    // ================================================================

    /**
     * 创建数字合约版本（EntityManager 直写 t_order_contract_version）。
     *
     * <p>无主数据空值不阻断下单：快照 JSON 中的空字段以占位符填充。
     *
     * @param orderHeaderId 订单头 ID
     * @param versionNo 版本号
     * @param snapshotJson 合约快照 JSON
     * @param userId 操作人用户 ID
     */
    private void createContractVersion(Long orderHeaderId, int versionNo, String snapshotJson, String userId) {
        try {
            entityManager.createNativeQuery(
                    "INSERT INTO t_order_contract_version "
                            + "(order_header_id, version_no, snapshot_json, create_time, create_by, update_time, update_by, del_flag) "
                            + "VALUES (:headerId, :versionNo, :snapshot, CURRENT_TIMESTAMP, :userId, CURRENT_TIMESTAMP, :userId, 0)")
                    .setParameter("headerId", orderHeaderId)
                    .setParameter("versionNo", versionNo)
                    .setParameter("snapshot", snapshotJson)
                    .setParameter("userId", userId)
                    .executeUpdate();
        } catch (Exception e) {
            log.warn("Failed to create contract version for orderHeaderId={}, versionNo={}: {}",
                    orderHeaderId, versionNo, e.getMessage());
        }
    }

    /**
     * 在状态变更时追加新版本合约。
     *
     * <p>自动计算下一版本号（当前最大版本号 + 1）。
     *
     * @param orderHeaderId 订单头 ID
     * @param header 订单头（用于构建快照）
     * @param userId 操作人用户 ID
     */
    private void appendContractVersion(Long orderHeaderId, TOrderHeaderEntity header, String userId) {
        int nextVersion = queryNextContractVersionNo(orderHeaderId);
        String snapshot = buildContractSnapshotJson(header);
        createContractVersion(orderHeaderId, nextVersion, snapshot, userId);
    }

    /**
     * 查询下一版本号（当前最大版本号 + 1）。
     *
     * @param orderHeaderId 订单头 ID
     * @return 下一版本号（默认 1）
     */
    private int queryNextContractVersionNo(Long orderHeaderId) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT MAX(version_no) FROM t_order_contract_version "
                            + "WHERE order_header_id = :headerId AND del_flag = 0")
                    .setParameter("headerId", orderHeaderId)
                    .getResultList();
            if (!results.isEmpty() && results.get(0) != null) {
                return ((Number) results.get(0)).intValue() + 1;
            }
        } catch (Exception e) {
            log.warn("Failed to query max contract version for orderHeaderId={}: {}",
                    orderHeaderId, e.getMessage());
        }
        return 1;
    }

    /**
     * 查询订单的合约版本列表（EntityManager 原生查询）。
     *
     * @param orderHeaderId 订单头 ID
     * @return 合约版本列表
     */
    private List<ContractVersionItem> queryContractVersions(Long orderHeaderId) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT version_no, CAST(snapshot_json AS VARCHAR), create_time FROM t_order_contract_version "
                            + "WHERE order_header_id = :headerId AND del_flag = 0 ORDER BY version_no ASC")
                    .setParameter("headerId", orderHeaderId)
                    .getResultList();
            return rows.stream()
                    .map(row -> new ContractVersionItem(
                            ((Number) row[0]).intValue(),
                            row[1] != null ? row[1].toString() : "",
                            row[2] != null ? ((java.sql.Timestamp) row[2]).toLocalDateTime() : null))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to query contract versions for orderHeaderId={}: {}",
                    orderHeaderId, e.getMessage());
            return List.of();
        }
    }

    /**
     * 构建合约快照 JSON（状态变更时写入）。
     *
     * <p>包含订单主数据 + 当前状态；无主数据空值不阻断（空值用占位符）。
     *
     * @param header 订单头
     * @return JSON 字符串
     */
    private String buildContractSnapshotJson(TOrderHeaderEntity header) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("orderId", header.getOrderNo());
        snapshot.put("status", header.getStatus());
        snapshot.put("productCode", header.getProductCode() != null ? header.getProductCode() : "");
        snapshot.put("amountTaxInclusive", header.getAmountTaxInclusive() != null
                ? header.getAmountTaxInclusive().toPlainString() : "0.00");
        snapshot.put("amountTaxExclusive", header.getAmountTaxExclusive() != null
                ? header.getAmountTaxExclusive().toPlainString() : "0.00");
        snapshot.put("currency", header.getCurrency() != null ? header.getCurrency() : "CNY");
        snapshot.put("demandUserId", header.getDemandUserId() != null ? header.getDemandUserId() : "");
        snapshot.put("providerEnterpriseId", header.getProviderEnterpriseId() != null
                ? header.getProviderEnterpriseId() : "");
        snapshot.put("snapshotTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize contract snapshot: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 判断状态是否可取消（三未完成态）。
     *
     * @param status 当前状态
     * @return 是否可取消
     */
    private boolean isCancellableState(OrderStatus status) {
        return status == OrderStatus.PENDING_CONFIRM
                || status == OrderStatus.PENDING_UPLOAD
                || status == OrderStatus.PENDING_CONTRACT_CONFIRM;
    }

    /**
     * 校验确认/取消操作的角色范围。
     *
     * <p>ADMIN 可代任意企业；PROVIDER 仅本企业；USER 仅本人。
     *
     * @param header 订单头
     * @param userId 操作人用户 ID
     * @param role 操作人角色
     * @return 是否有权限
     */
    private boolean canConfirmOrCancelOrder(TOrderHeaderEntity header, String userId, Role role) {
        return switch (role) {
            case ADMIN -> true;
            case PROVIDER -> {
                Long enterpriseIdLong = safeParseLong(userId).orElse(null);
                if (enterpriseIdLong == null) {
                    yield false;
                }
                SysUserEntity user = sysUserRepo.findById(enterpriseIdLong).orElse(null);
                String enterpriseId = user != null ? String.valueOf(user.getEnterpriseId()) : "";
                yield enterpriseId.equals(header.getProviderEnterpriseId());
            }
            case USER -> userId.equals(header.getDemandUserId());
        };
    }

    /**
     * 构建列表项响应（创建时使用，已知产品信息）。
     */
    private OrderListItemResponse buildListItemResponse(TOrderHeaderEntity header,
                                                         DataProductEntity product,
                                                         String providerEnterpriseName,
                                                         String demandUserName,
                                                         String demandUserLoginName,
                                                         String demandEnterpriseName) {
        OrderStatus status = OrderStatus.valueOf(header.getStatus());
        return new OrderListItemResponse(
                header.getOrderNo(),
                product.getProductName(),
                product.getProductCode(),
                product.getProductType(),
                "", // sourcePlatform
                providerEnterpriseName,
                demandUserName,
                demandUserLoginName,
                demandEnterpriseName,
                header.getAmountTaxInclusive(),
                header.getAmountTaxExclusive(),
                header.getCurrency(),
                status,
                status.getLabel(),
                header.getChainCount(),
                header.getCreateTime());
    }

    /**
     * 从订单头构建列表项响应（状态变更后使用）。
     */
    private OrderListItemResponse buildListItemResponseFromHeader(TOrderHeaderEntity header) {
        // 从快照取产品信息
        String productName = "";
        String productCode = header.getProductCode();
        String productType = "";
        if (header.getProductSnapshot() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> snap = objectMapper.readValue(header.getProductSnapshot(), Map.class);
                productName = (String) snap.getOrDefault("productName", "");
                productType = (String) snap.getOrDefault("productType", "");
            } catch (JsonProcessingException e) {
                // 使用空值
            }
        }

        // 获取需求方信息
        SysUserEntity demandUser = safeParseLong(header.getDemandUserId())
                .flatMap(sysUserRepo::findById).orElse(null);
        String demandUserName = demandUser != null ? demandUser.getDisplayName() : "";
        String demandUserLoginName = demandUser != null ? demandUser.getUsername() : "";
        String demandEnterpriseName = demandUser != null ? demandUser.getEnterpriseName() : "";

        // 获取提供方企业名称
        String providerEnterpriseName = "";
        EnterpriseEntity providerEnt = safeParseLong(header.getProviderEnterpriseId())
                .flatMap(id -> enterpriseRepo.findByIdAndDelFlag(id, false)).orElse(null);
        if (providerEnt != null) {
            providerEnterpriseName = providerEnt.getName();
        }

        OrderStatus status = OrderStatus.valueOf(header.getStatus());
        return new OrderListItemResponse(
                header.getOrderNo(),
                productName,
                productCode,
                productType,
                "",
                providerEnterpriseName,
                demandUserName,
                demandUserLoginName,
                demandEnterpriseName,
                header.getAmountTaxInclusive(),
                header.getAmountTaxExclusive(),
                header.getCurrency(),
                status,
                status.getLabel(),
                header.getChainCount(),
                header.getCreateTime());
    }

    /**
     * 按角色与状态分页查询订单（原生 SQL，MySQL JSON_EXTRACT）。
     *
     * <p>在 H2 测试环境下会失败（JSON_EXTRACT / JSON_UNQUOTE 不支持），
     * 调用方需捕获异常并在 REQUIRES_NEW 事务中执行 fallback。
     */
    private Page<TOrderHeaderEntity> queryOrdersByRoleNative(String userId, Role role,
                                                              String status, String keyword,
                                                              Pageable pageable) {
        return switch (role) {
            case USER -> orderHeaderRepo.searchUserOrders(userId, status, keyword, pageable);
            case PROVIDER -> {
                Long enterpriseIdLong;
                try {
                    enterpriseIdLong = Long.parseLong(userId);
                } catch (NumberFormatException e) {
                    log.warn("Invalid userId format for PROVIDER role: {}", userId);
                    yield Page.empty(pageable);
                }
                SysUserEntity user = sysUserRepo.findById(enterpriseIdLong).orElse(null);
                String enterpriseId = user != null ? String.valueOf(user.getEnterpriseId()) : "";
                yield orderHeaderRepo.searchProviderOrders(enterpriseId, status, keyword, pageable);
            }
            case ADMIN -> orderHeaderRepo.searchAdminOrders(status, keyword, pageable);
        };
    }

    /**
     * 按角色与状态分页查询订单（JPQL fallback，H2 兼容，仅支持状态筛选）。
     */
    private Page<TOrderHeaderEntity> queryOrdersByRoleFallback(String userId, Role role,
                                                                String status, Pageable pageable) {
        return switch (role) {
            case USER -> orderHeaderRepo.searchUserOrdersFallback(userId, status, pageable);
            case PROVIDER -> {
                Long enterpriseIdLong;
                try {
                    enterpriseIdLong = Long.parseLong(userId);
                } catch (NumberFormatException ex) {
                    log.warn("Invalid userId format for PROVIDER role: {}", userId);
                    yield Page.empty(pageable);
                }
                SysUserEntity user = sysUserRepo.findById(enterpriseIdLong).orElse(null);
                String enterpriseId = user != null ? String.valueOf(user.getEnterpriseId()) : "";
                yield orderHeaderRepo.searchProviderOrdersFallback(enterpriseId, status, pageable);
            }
            case ADMIN -> orderHeaderRepo.searchAdminOrdersFallback(status, pageable);
        };
    }

    /**
     * 组装订单列表响应（批量富化 + 列表项构建）。
     */
    private ServiceResult assembleListResponse(Page<TOrderHeaderEntity> resultPage, int page, int pageSize) {
        List<TOrderHeaderEntity> pageItems = resultPage.getContent();

        // 批量预取当前页涉及的用户与企业
        Set<String> pageUserIds = pageItems.stream()
                .map(TOrderHeaderEntity::getDemandUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUserEntity> pageUserMap = batchFetchUsers(pageUserIds);

        Set<String> pageEntIds = pageItems.stream()
                .map(TOrderHeaderEntity::getProviderEnterpriseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, EnterpriseEntity> pageEntMap = batchFetchEnterprises(pageEntIds);

        // 组装列表项
        List<OrderListItemResponse> items = pageItems.stream()
                .map(o -> toListItem(o, pageUserMap, pageEntMap))
                .collect(Collectors.toList());

        OrderPageResponse pageResponse = new OrderPageResponse(
                (int) resultPage.getTotalElements(), items, page, pageSize);
        return ServiceResult.ok(pageResponse);
    }

    /**
     * 查找订单头（支持 orderNo 或 id）。
     */
    private TOrderHeaderEntity findOrderHeader(String orderId) {
        Optional<TOrderHeaderEntity> byNo = orderHeaderRepo.findByOrderNo(orderId);
        if (byNo.isPresent()) {
            return byNo.get();
        }
        try {
            Long id = Long.parseLong(orderId);
            return orderHeaderRepo.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 检查用户是否有权访问该订单（列表/详情查看权限）。
     */
    private boolean canAccessOrder(TOrderHeaderEntity header, String userId, Role role) {
        return switch (role) {
            case ADMIN -> true;
            case PROVIDER -> {
                Long enterpriseIdLong = safeParseLong(userId).orElse(null);
                if (enterpriseIdLong == null) {
                    yield false;
                }
                SysUserEntity user = sysUserRepo.findById(enterpriseIdLong).orElse(null);
                String enterpriseId = user != null ? String.valueOf(user.getEnterpriseId()) : "";
                yield enterpriseId.equals(header.getProviderEnterpriseId());
            }
            case USER -> userId.equals(header.getDemandUserId());
        };
    }

    /**
     * 将订单头实体转换为列表项响应。
     */
    private OrderListItemResponse toListItem(TOrderHeaderEntity header,
                                              Map<Long, SysUserEntity> userMap,
                                              Map<Long, EnterpriseEntity> enterpriseMap) {
        // 从快照取产品信息
        String productName = "";
        String productCode = header.getProductCode();
        String productType = "";
        String sourcePlatform = "";
        if (header.getProductSnapshot() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> snap = objectMapper.readValue(
                        header.getProductSnapshot(), Map.class);
                productName = (String) snap.getOrDefault("productName", "");
                productType = (String) snap.getOrDefault("productType", "");
                sourcePlatform = (String) snap.getOrDefault("sourcePlatform", "");
            } catch (JsonProcessingException e) {
                // 使用空值
            }
        }

        // 获取需求方信息
        String demandUserName = "";
        String demandUserLoginName = "";
        String demandEnterpriseName = "";
        SysUserEntity demandUser = safeParseLong(header.getDemandUserId())
                .map(userMap::get).orElse(null);
        if (demandUser != null) {
            demandUserName = demandUser.getDisplayName();
            demandUserLoginName = demandUser.getUsername();
            demandEnterpriseName = demandUser.getEnterpriseName();
        }

        // 获取提供方企业名称
        String providerEnterpriseName = "";
        EnterpriseEntity providerEnt = safeParseLong(header.getProviderEnterpriseId())
                .map(enterpriseMap::get).orElse(null);
        if (providerEnt != null) {
            providerEnterpriseName = providerEnt.getName();
        }

        OrderStatus status = OrderStatus.valueOf(header.getStatus());

        return new OrderListItemResponse(
                header.getOrderNo(),
                productName,
                productCode,
                productType,
                sourcePlatform,
                providerEnterpriseName,
                demandUserName,
                demandUserLoginName,
                demandEnterpriseName,
                header.getAmountTaxInclusive(),
                header.getAmountTaxExclusive(),
                header.getCurrency(),
                status,
                status.getLabel(),
                header.getChainCount(),
                header.getCreateTime());
    }

    /**
     * 将订单头实体转换为详情响应（含附件、交易信息、时间线、mock 上链记录）。
     */
    private OrderDetailResponse toDetailResponse(TOrderHeaderEntity header) {
        // 产品快照
        OrderProductSnapshot productSnapshot = parseProductSnapshot(header.getProductSnapshot());

        // 参与方
        OrderParticipant participant = buildParticipant(header);

        // 附件元数据
        OrderAttachmentMeta attachmentMeta = buildAttachmentMeta(header.getId());

        // 交易信息
        OrderTransactionInfo transactionInfo = buildTransactionInfo(header.getId());

        // 时间线
        List<TOrderEventEntity> events = orderEventRepo.findByOrderHeaderIdOrderByIdAsc(header.getId());
        List<OrderTimelineEventItem> timeline = events.stream()
                .map(e -> new OrderTimelineEventItem(
                        e.getEventType(),
                        e.getDescription(),
                        e.getOperatorId(),
                        e.getOperatorRole(),
                        e.getCreateTime()))
                .collect(Collectors.toList());

        // mock 上链记录
        List<TOrderChainLogEntity> chainLogs = orderChainLogRepo.findByOrderHeaderIdOrderByIdAsc(header.getId());
        List<OrderChainLogItem> chainLogItems = chainLogs.stream()
                .map(c -> new OrderChainLogItem(
                        c.getChainHash(),
                        c.getBlockHeight(),
                        c.getChainNode(),
                        c.getChainTimestamp()))
                .collect(Collectors.toList());

        // 数字合约版本
        List<ContractVersionItem> contractVersions = queryContractVersions(header.getId());

        OrderStatus status = OrderStatus.valueOf(header.getStatus());

        return new OrderDetailResponse(
                header.getOrderNo(),
                status,
                status.getLabel(),
                productSnapshot,
                participant,
                header.getRemark(),
                header.getNoticeVersion(),
                transactionInfo,
                attachmentMeta,
                timeline,
                chainLogItems,
                contractVersions,
                header.getChainCount(),
                header.getAmountTaxInclusive(),
                header.getAmountTaxExclusive(),
                header.getCurrency(),
                header.getCreateTime(),
                header.getUpdateTime());
    }

    /**
     * 构建附件元数据。
     *
     * @param orderHeaderId 订单头 ID
     * @return 附件元数据或 null
     */
    private OrderAttachmentMeta buildAttachmentMeta(Long orderHeaderId) {
        List<TOrderAttachmentEntity> attachments = orderAttachmentRepo.findByOrderHeaderId(orderHeaderId);
        if (attachments.isEmpty()) {
            return null;
        }
        TOrderAttachmentEntity att = attachments.get(0);
        return new OrderAttachmentMeta(
                att.getFileName(),
                att.getFileType(),
                att.getFileSize(),
                att.getScanResult());
    }

    /**
     * 构建交易信息。
     *
     * @param orderHeaderId 订单头 ID
     * @return 交易信息或 null
     */
    private OrderTransactionInfo buildTransactionInfo(Long orderHeaderId) {
        List<TOrderLineEntity> lines = orderLineRepo.findByOrderHeaderId(orderHeaderId);
        if (lines.isEmpty()) {
            return null;
        }
        List<OrderLineItem> lineItems = lines.stream()
                .map(l -> new OrderLineItem(l.getUnit(), l.getQuantity(), l.getUnitPrice(), l.getSubtotal()))
                .collect(Collectors.toList());

        // 从订单头获取金额（含税=未税）
        TOrderHeaderEntity header = orderHeaderRepo.findById(orderHeaderId).orElse(null);
        BigDecimal amountInclusive = header != null ? header.getAmountTaxInclusive() : BigDecimal.ZERO;
        BigDecimal amountExclusive = header != null ? header.getAmountTaxExclusive() : BigDecimal.ZERO;
        String currency = header != null ? header.getCurrency() : "CNY";

        return new OrderTransactionInfo(lineItems, amountInclusive, amountExclusive, currency);
    }

    /**
     * 解析产品快照 JSON。
     */
    private OrderProductSnapshot parseProductSnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return new OrderProductSnapshot("", "", "", "", "", null);
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> snap = objectMapper.readValue(snapshotJson, Map.class);
            return new OrderProductSnapshot(
                    String.valueOf(snap.getOrDefault("productId", "")),
                    String.valueOf(snap.getOrDefault("productName", "")),
                    String.valueOf(snap.getOrDefault("productCode", "")),
                    String.valueOf(snap.getOrDefault("productType", "")),
                    String.valueOf(snap.getOrDefault("sourcePlatform", "")),
                    (Boolean) snap.get("allowSimpleOrder"));
        } catch (JsonProcessingException e) {
            return new OrderProductSnapshot("", "", "", "", "", null);
        }
    }

    /**
     * 构建参与方信息。
     */
    private OrderParticipant buildParticipant(TOrderHeaderEntity header) {
        // 需求方
        SysUserEntity demandUser = safeParseLong(header.getDemandUserId())
                .flatMap(sysUserRepo::findById).orElse(null);
        String demandUserName = demandUser != null ? demandUser.getDisplayName() : "";
        String demandUserLoginName = demandUser != null ? demandUser.getUsername() : "";
        String demandEnterpriseId = demandUser != null ? String.valueOf(demandUser.getEnterpriseId()) : "";
        String demandEnterpriseName = demandUser != null ? demandUser.getEnterpriseName() : "";

        // 提供方
        String providerEnterpriseName = "";
        EnterpriseEntity providerEnt = safeParseLong(header.getProviderEnterpriseId())
                .flatMap(id -> enterpriseRepo.findByIdAndDelFlag(id, false)).orElse(null);
        if (providerEnt != null) {
            providerEnterpriseName = providerEnt.getName();
        }

        return new OrderParticipant(
                header.getProviderEnterpriseId(),
                providerEnterpriseName,
                header.getDemandUserId(),
                demandUserName,
                demandUserLoginName,
                demandEnterpriseId,
                demandEnterpriseName);
    }

    /**
     * 批量预取用户实体（避免逐条查询 N+1）。
     */
    private Map<Long, SysUserEntity> batchFetchUsers(Set<String> userIds) {
        List<Long> ids = userIds.stream()
                .map(this::safeParseLong)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return sysUserRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(SysUserEntity::getId, u -> u));
    }

    /**
     * 批量预取企业实体（避免逐条查询 N+1）。
     */
    private Map<Long, EnterpriseEntity> batchFetchEnterprises(Set<String> enterpriseIds) {
        List<Long> ids = enterpriseIds.stream()
                .map(this::safeParseLong)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return enterpriseRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(EnterpriseEntity::getId, e -> e));
    }

    /**
     * 安全解析 Long，避免 NumberFormatException。
     */
    private Optional<Long> safeParseLong(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
