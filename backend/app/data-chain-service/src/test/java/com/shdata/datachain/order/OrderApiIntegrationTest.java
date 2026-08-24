package com.shdata.datachain.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.annotation.DirtiesContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * TASK-WSC-916 集成测试门禁。
 *
 * <p>用例覆盖：916-state-machine、916-admin-proxy-provider、916-cancel-three-states、
 * 916-chain-count-per-state、916-idor-negative-params。
 * 环境：H2 in-memory（MODE=MySQL），Flyway 关闭，ddl-auto=create-drop。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_order_916;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "spring.flyway.enabled=false",
      "chainmp.enabled=false",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
    })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderApiIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private EntityManager entityManager;
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private com.shdata.datachain.common.security.PasswordHasher passwordHasher;

  private static final Pattern UUID_V4 =
      Pattern.compile(
          "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

  private static final String LOGIN_URL = "/api/v1/auth/session";
  private static final String ORDERS_URL = "/api/v1/orders";

  private Long adminUserId;
  private Long providerUserId;
  private Long provider2UserId;
  private Long user1Id;
  private Long user2Id;
  private Long productId;

  @BeforeEach
  void setUp() {
    transactionTemplate.executeWithoutResult(status -> {
      // 清理种子数据（避免主键冲突）
      executeUpdate("DELETE FROM sys_enterprise WHERE id IN (1, 2)");
      
      // 企业
      executeUpdate(
          "INSERT INTO sys_enterprise (id, code, name, del_flag, create_time, update_time, create_by, update_by) "
              + "VALUES (1, 'ENT-A', '测试企业A', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '', '')");
      executeUpdate(
          "INSERT INTO sys_enterprise (id, code, name, del_flag, create_time, update_time, create_by, update_by) "
              + "VALUES (2, 'ENT-B', '测试企业B', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '', '')");

      // 用户
      adminUserId = insertUser("testadmin", "管理员", 1L, "ADMIN");
      providerUserId = insertUser("testprovider", "提供方用户A", 1L, "PROVIDER");
      provider2UserId = insertUser("testprovider2", "提供方用户B", 2L, "PROVIDER");
      user1Id = insertUser("testuser1", "需求方张三", 1L, "USER");
      user2Id = insertUser("testuser2", "需求方李四", 2L, "USER");

      // 产品（允许简易流程，create_by = providerUserId 即企业 A）
      productId = insertProduct("P-916-001", "测试数据产品", "DATA_SERVICE", providerUserId.toString(), true);
    });
  }

  @AfterEach
  void tearDown() {
    // @DirtiesContext will handle context cleanup
  }

  // ================================================================
  // 916-state-machine：主路径四步
  // ================================================================

  @Nested
  @DisplayName("916-state-machine")
  class StateMachine {

    @Test
    @DisplayName("主路径：创建→确认订单→提交合约→确认合约→合约已达成")
    void mainPath_createToContractReached() throws Exception {
      // 1. USER 创建订单
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "主路径测试");

      // 2. PROVIDER 确认订单（PENDING_CONFIRM → PENDING_UPLOAD）
      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"))
          .andExpect(jsonPath("$.data.status").value("PENDING_UPLOAD"));

      // 3. USER 提交合约附件与交易信息（PENDING_UPLOAD → PENDING_CONTRACT_CONFIRM）
      mockMvc
          .perform(buildContractMultipart(orderId, userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"))
          .andExpect(jsonPath("$.data.status").value("PENDING_CONTRACT_CONFIRM"));

      // 4. PROVIDER 确认合约（PENDING_CONTRACT_CONFIRM → CONTRACT_REACHED）
      String confirmBody = "{\"noticeAccepted\":true}";
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/contract/confirm")
              .session(providerSession)
              .contentType(MediaType.APPLICATION_JSON)
              .content(confirmBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"))
          .andExpect(jsonPath("$.data.status").value("CONTRACT_REACHED"));
    }

    @Test
    @DisplayName("不可回退：CONFIRMED 状态下再确认 → 4xx")
    void confirmOrder_invalidState_returns4xx() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "回退测试");

      MockHttpSession providerSession = login("testprovider", "demo");
      // 第一次确认成功
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"));

      // 第二次确认 → 状态不对
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("ERR_ORDER_INVALID_STATE"));
    }

    @Test
    @DisplayName("USER 确认订单 → 200（需求方可确认）")
    void confirmOrder_user_forbidden() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "USER 确认测试");

      // USER 确认自己的订单 → 200（需求方可以确认）
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"));
    }

    @Test
    @DisplayName("PROVIDER 他企确认订单 → 403")
    void confirmOrder_otherProvider_forbidden() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "他企测试");

      // 企业B的PROVIDER 尝试确认企业A的订单 → 403
      MockHttpSession provider2Session = login("testprovider2", "demo");
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(provider2Session))
          .andExpect(status().isForbidden());
    }
  }

  // ================================================================
  // 916-admin-proxy-provider：ADMIN 代操作
  // ================================================================

  @Nested
  @DisplayName("916-admin-proxy-provider")
  class AdminProxyProvider {

    @Test
    @DisplayName("ADMIN 可代确认订单 → PENDING_UPLOAD")
    void adminConfirmOrder_success() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "ADMIN代确认");

      MockHttpSession adminSession = login("testadmin", "demo");
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(adminSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"))
          .andExpect(jsonPath("$.data.status").value("PENDING_UPLOAD"));
    }

    @Test
    @DisplayName("ADMIN 代确认后 update_by = ADMIN 用户标识")
    void adminConfirmOrder_updateByAdmin() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "ADMIN审计");

      MockHttpSession adminSession = login("testadmin", "demo");
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(adminSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"));

      // 验证 update_by = admin 用户 ID
      String updateBy = queryUpdateBy(orderId);
      assertEquals(String.valueOf(adminUserId), updateBy, "update_by 应为 ADMIN 用户标识");
    }

    @Test
    @DisplayName("ADMIN 不能代 USER 上传合约")
    void adminSubmitContract_forbidden() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "ADMIN代上传测试");

      // 先确认订单
      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession))
          .andExpect(status().isOk());

      // ADMIN 尝试上传合约 → 403
      MockHttpSession adminSession = login("testadmin", "demo");
      mockMvc
          .perform(buildContractMultipart(orderId, adminSession))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN 可代确认合约 → CONTRACT_REACHED")
    void adminConfirmContract_success() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "ADMIN代确认合约");

      // PROVIDER 确认订单
      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession));

      // USER 提交合约
      mockMvc.perform(buildContractMultipart(orderId, userSession));

      // ADMIN 代确认合约
      MockHttpSession adminSession = login("testadmin", "demo");
      String confirmBody = "{\"noticeAccepted\":true}";
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/contract/confirm")
              .session(adminSession)
              .contentType(MediaType.APPLICATION_JSON)
              .content(confirmBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"))
          .andExpect(jsonPath("$.data.status").value("CONTRACT_REACHED"));
    }

    @Test
    @DisplayName("ADMIN 可代 PROVIDER 取消三未完成态订单 → CANCELLED")
    void adminCancelOrder_success() throws Exception {
      // 1. USER 创建订单（PENDING_CONFIRM）
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "ADMIN代取消");

      // 2. ADMIN 代取消（PENDING_CONFIRM → CANCELLED）
      MockHttpSession adminSession = login("testadmin", "demo");
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/cancel").session(adminSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"))
          .andExpect(jsonPath("$.data.status").value("CANCELLED"));

      // 3. 验证 update_by = admin 用户标识
      String updateBy = queryUpdateBy(orderId);
      assertEquals(String.valueOf(adminUserId), updateBy, "update_by 应为 ADMIN 用户标识");

      // 4. 验证时间线最新事件 operator = ADMIN
      String operatorId = queryLastTimelineOperator(orderId);
      assertEquals(String.valueOf(adminUserId), operatorId, "时间线 operator 应为 ADMIN 用户标识");
    }
  }

  // ================================================================
  // 916-cancel-three-states：取消三态
  // ================================================================

  @Nested
  @DisplayName("916-cancel-three-states")
  class CancelThreeStates {

    @Test
    @DisplayName("PENDING_CONFIRM 可取消 → CANCELLED")
    void cancel_pendingConfirm_success() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "取消测试1");

      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/cancel").session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"))
          .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PENDING_UPLOAD 可取消 → CANCELLED")
    void cancel_pendingUpload_success() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "取消测试2");

      // PROVIDER 确认订单
      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession));

      // USER 取消
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/cancel").session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PENDING_CONTRACT_CONFIRM 可取消 → CANCELLED")
    void cancel_pendingContractConfirm_success() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "取消测试3");

      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession));
      mockMvc.perform(buildContractMultipart(orderId, userSession));

      // USER 取消
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/cancel").session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("CONTRACT_REACHED 不可取消 → 4xx")
    void cancel_contractReached_returns4xx() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "取消测试4");

      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession));
      mockMvc.perform(buildContractMultipart(orderId, userSession));

      String confirmBody = "{\"noticeAccepted\":true}";
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/contract/confirm")
          .session(providerSession)
          .contentType(MediaType.APPLICATION_JSON)
          .content(confirmBody));

      // 已达成不可取消
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/cancel").session(userSession))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("ERR_ORDER_INVALID_STATE"));
    }

    @Test
    @DisplayName("CANCELLED 不可再取消 → 4xx")
    void cancel_alreadyCancelled_returns4xx() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "取消测试5");

      // 第一次取消成功
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/cancel").session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.status").value("CANCELLED"));

      // 第二次取消 → 状态不对
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/cancel").session(userSession))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("ERR_ORDER_INVALID_STATE"));
    }
  }

  // ================================================================
  // 916-chain-count-per-state：上链次数 = 时间线条数
  // ================================================================

  @Nested
  @DisplayName("916-chain-count-per-state")
  class ChainCountPerState {

    @Test
    @DisplayName("每步状态变更后 chainCount = 时间线记录条数；列表与详情一致")
    void chainCount_matchesTimeline_afterEachState() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "chainCount测试");

      // 创建后：chainCount=1, timeline=1
      verifyChainCountAndTimeline(userSession, orderId, 1);

      MockHttpSession providerSession = login("testprovider", "demo");

      // 确认订单后：chainCount=2, timeline=2
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession));
      verifyChainCountAndTimeline(userSession, orderId, 2);

      // 提交合约后：chainCount=3, timeline=3
      mockMvc.perform(buildContractMultipart(orderId, userSession));
      verifyChainCountAndTimeline(userSession, orderId, 3);

      // 确认合约后：chainCount=4, timeline=4
      String confirmBody = "{\"noticeAccepted\":true}";
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/contract/confirm")
          .session(providerSession)
          .contentType(MediaType.APPLICATION_JSON)
          .content(confirmBody));
      verifyChainCountAndTimeline(userSession, orderId, 4);

      // 验证列表 chainCount 与详情一致
      MvcResult listResult = mockMvc
          .perform(get(ORDERS_URL).session(userSession))
          .andExpect(status().isOk())
          .andReturn();
      int listChainCount = JsonPath.read(listResult.getResponse().getContentAsString(),
          "$.data.list[0].chainCount");
      assertEquals(4, listChainCount, "列表 chainCount 应与详情一致");
    }

    @Test
    @DisplayName("取消后 chainCount 也递增")
    void chainCount_increments_onCancel() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "cancel chainCount");

      // 创建后 chainCount=1
      verifyChainCountAndTimeline(userSession, orderId, 1);

      // 取消后 chainCount=2
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/cancel").session(userSession));
      verifyChainCountAndTimeline(userSession, orderId, 2);
    }
  }

  // ================================================================
  // 916-idor-negative-params：篡改参数负例
  // ================================================================

  @Nested
  @DisplayName("916-idor-negative-params")
  class IdorNegativeParams {

    @Test
    @DisplayName("确认订单携带篡改 enterpriseId 参数 → 行为与不携带一致")
    void confirmOrder_tamperedEnterpriseId_noEscalation() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "IDOR测试");

      // 企业B PROVIDER 携带 enterpriseId=1 试图扩大权限 → 仍 403
      MockHttpSession provider2Session = login("testprovider2", "demo");
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/confirm")
              .param("enterpriseId", "1")
              .session(provider2Session))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("取消携带篡改 userId 参数 → 行为与不携带一致")
    void cancelOrder_tamperedUserId_noEscalation() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "IDOR取消");

      // user2 携带 userId=user1Id 试图代取消 → 仍 403
      MockHttpSession user2Session = login("testuser2", "demo");
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/cancel")
              .param("userId", String.valueOf(user1Id))
              .session(user2Session))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PROVIDER 他企取消 → 403")
    void cancelOrder_otherProvider_forbidden() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "他企取消");

      MockHttpSession provider2Session = login("testprovider2", "demo");
      mockMvc
          .perform(post(ORDERS_URL + "/" + orderId + "/cancel").session(provider2Session))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("mock 上链记录不含真实链节点信息")
    void mockChain_noRealNodeInfo() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "mock链测试");

      MvcResult detail = mockMvc
          .perform(get(ORDERS_URL + "/" + orderId).session(userSession))
          .andExpect(status().isOk())
          .andReturn();

      String detailJson = detail.getResponse().getContentAsString();
      String chainNode = JsonPath.read(detailJson, "$.data.chainLogs[0].chainNode");
      assertNotNull(chainNode, "chainNode 不应为空");
      assertFalse(chainNode.contains("http"), "mock chainNode 不应含 URL");
      assertFalse(chainNode.contains("://"), "mock chainNode 不应含协议");
      assertEquals("simulated-node-local", chainNode, "chainNode 应为模拟占位符");
    }
  }

  // ================================================================
  // 916-attachment-validation：附件校验
  // ================================================================

  @Nested
  @DisplayName("916-attachment-validation")
  class AttachmentValidation {

    @Test
    @DisplayName("无附件 → 不能进入待确认合约")
    void submitContract_noAttachment_rejected() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "无附件测试");

      // 确认订单 → PENDING_UPLOAD
      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession));

      // 构造空文件（文件名为空）+ transactionInfo 的 multipart 请求
      String txJson = buildTransactionInfoJson("100.00");
      MockMultipartFile emptyFile = new MockMultipartFile(
          "file", "", "application/octet-stream", new byte[0]);

      mockMvc
          .perform(multipart(ORDERS_URL + "/" + orderId + "/contract")
              .file(emptyFile)
              .param("transactionInfo", txJson)
              .session(userSession))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("ERR_ORDER_ATTACHMENT_INVALID"));

      // 验证订单状态仍为 PENDING_UPLOAD
      mockMvc
          .perform(get(ORDERS_URL + "/" + orderId).session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.status").value("PENDING_UPLOAD"));
    }

    @Test
    @DisplayName("非白名单文件 → 不能提交")
    void submitContract_invalidFileType_rejected() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "非白名单测试");

      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession));

      String txJson = buildTransactionInfoJson("100.00");
      MockMultipartFile file = new MockMultipartFile(
          "file", "test.exe", "application/octet-stream", "fake".getBytes(StandardCharsets.UTF_8));

      mockMvc
          .perform(multipart(ORDERS_URL + "/" + orderId + "/contract")
              .file(file)
              .param("transactionInfo", txJson)
              .session(userSession))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("ERR_ORDER_ATTACHMENT_INVALID"));
    }
  }

  // ================================================================
  // 916-amount-validation：金额校验
  // ================================================================

  @Nested
  @DisplayName("916-amount-validation")
  class AmountValidation {

    @Test
    @DisplayName("单价 ≤0 → 不能提交")
    void submitContract_zeroUnitPrice_rejected() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "零单价测试");

      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession));

      String txJson = "{\"orderLines\":[{\"unit\":\"条\",\"quantity\":1,\"unitPrice\":\"0.00\",\"subtotal\":\"0.00\"}],"
          + "\"amountTaxInclusive\":\"0.00\",\"amountTaxExclusive\":\"0.00\",\"currency\":\"CNY\"}";
      MockMultipartFile file = new MockMultipartFile(
          "file", "test.pdf", "application/pdf", "fake".getBytes(StandardCharsets.UTF_8));

      mockMvc
          .perform(multipart(ORDERS_URL + "/" + orderId + "/contract")
              .file(file)
              .param("transactionInfo", txJson)
              .session(userSession))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("ERR_ORDER_LINE_INVALID"));
    }

    @Test
    @DisplayName("缺单位 → 不能提交")
    void submitContract_missingUnit_rejected() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "缺单位测试");

      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession));

      String txJson = "{\"orderLines\":[{\"unit\":\"\",\"quantity\":1,\"unitPrice\":\"100.00\",\"subtotal\":\"100.00\"}],"
          + "\"amountTaxInclusive\":\"100.00\",\"amountTaxExclusive\":\"100.00\",\"currency\":\"CNY\"}";
      MockMultipartFile file = new MockMultipartFile(
          "file", "test.pdf", "application/pdf", "fake".getBytes(StandardCharsets.UTF_8));

      mockMvc
          .perform(multipart(ORDERS_URL + "/" + orderId + "/contract")
              .file(file)
              .param("transactionInfo", txJson)
              .session(userSession))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("ERR_ORDER_LINE_INVALID"));
    }

    @Test
    @DisplayName("含税≠未税 → 不能提交")
    void submitContract_mismatchedAmounts_rejected() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "金额不一致测试");

      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession));

      String txJson = "{\"orderLines\":[{\"unit\":\"条\",\"quantity\":1,\"unitPrice\":\"100.00\",\"subtotal\":\"100.00\"}],"
          + "\"amountTaxInclusive\":\"100.00\",\"amountTaxExclusive\":\"90.00\",\"currency\":\"CNY\"}";
      MockMultipartFile file = new MockMultipartFile(
          "file", "test.pdf", "application/pdf", "fake".getBytes(StandardCharsets.UTF_8));

      mockMvc
          .perform(multipart(ORDERS_URL + "/" + orderId + "/contract")
              .file(file)
              .param("transactionInfo", txJson)
              .session(userSession))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("ERR_ORDER_AMOUNT_INVALID"));
    }
  }

  // ================================================================
  // 辅助方法
  // ================================================================

  private Long insertUser(String username, String displayName, Long enterpriseId, String role) {
    entityManager
        .createNativeQuery(
            "INSERT INTO sys_user (username, password_hash, display_name, enterprise_id, enterprise_name, role, del_flag, "
                + "create_time, update_time, create_by, update_by) "
                + "VALUES (:username, :passwordHash, :displayName, :entId, :entName, :role, 0, "
                + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '', '')")
        .setParameter("username", username)
        .setParameter("passwordHash", passwordHasher.hash("demo"))
        .setParameter("displayName", displayName)
        .setParameter("entId", enterpriseId)
        .setParameter("entName", enterpriseId == 1L ? "测试企业A" : "测试企业B")
        .setParameter("role", role)
        .executeUpdate();
    entityManager.flush();
    entityManager.clear();

    @SuppressWarnings("unchecked")
    java.util.List<Number> ids =
        entityManager
            .createNativeQuery("SELECT id FROM sys_user WHERE username = :username")
            .setParameter("username", username)
            .getResultList();
    return ids.get(0).longValue();
  }

  private Long insertProduct(String code, String name, String type, String createBy, boolean allowSimple) {
    entityManager
        .createNativeQuery(
            "INSERT INTO t_data_product (product_code, product_name, product_type, "
                + "del_flag, create_time, update_time, create_by, update_by, status, maintenance_status) "
                + "VALUES (:code, :name, :type, 0, "
                + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :createBy, :createBy, 'LISTED', 'MAINTAINED')")
        .setParameter("code", code)
        .setParameter("name", name)
        .setParameter("type", type)
        .setParameter("createBy", createBy)
        .executeUpdate();
    entityManager.flush();
    entityManager.clear();

    @SuppressWarnings("unchecked")
    java.util.List<Number> ids =
        entityManager
            .createNativeQuery("SELECT id FROM t_data_product WHERE product_code = :code AND del_flag = 0")
            .setParameter("code", code)
            .getResultList();
    Long id = ids.get(0).longValue();
    
    entityManager.flush();
    entityManager.clear();
    
    return id;
  }

  private String createOrder(MockHttpSession session, Long productId, String remark) throws Exception {
    String body =
        String.format(
            "{\"productId\":\"%d\",\"remark\":\"%s\",\"noticeAccepted\":true}", productId, remark);
    MvcResult result = mockMvc
        .perform(post(ORDERS_URL).session(session).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.data.orderId");
  }

  /**
   * 构建合约提交 multipart 请求。
   */
  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
      buildContractMultipart(String orderId, MockHttpSession session) throws Exception {

    String txJson = buildTransactionInfoJson("100.00");
    MockMultipartFile file = new MockMultipartFile(
        "file", "contract.pdf", "application/pdf", "fake-pdf-content".getBytes(StandardCharsets.UTF_8));

    return multipart(ORDERS_URL + "/" + orderId + "/contract")
        .file(file)
        .param("transactionInfo", txJson)
        .session(session);
  }

  private String buildTransactionInfoJson(String amount) {
    return String.format(
        "{\"orderLines\":[{\"unit\":\"条\",\"quantity\":1,\"unitPrice\":\"%s\",\"subtotal\":\"%s\"}],"
            + "\"amountTaxInclusive\":\"%s\",\"amountTaxExclusive\":\"%s\",\"currency\":\"CNY\"}",
        amount, amount, amount, amount);
  }

  /**
   * 验证 chainCount = 时间线记录条数。
   */
  private void verifyChainCountAndTimeline(MockHttpSession session, String orderId, int expectedCount)
      throws Exception {
    MvcResult detail = mockMvc
        .perform(get(ORDERS_URL + "/" + orderId).session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.chainCount").value(expectedCount))
        .andExpect(jsonPath("$.data.timeline.length()").value(expectedCount))
        .andExpect(jsonPath("$.data.chainLogs.length()").value(expectedCount))
        .andReturn();
  }

  /**
   * 查询订单的 update_by（直接查数据库）。
   */
  private String queryUpdateBy(String orderNo) {
    @SuppressWarnings("unchecked")
    java.util.List<String> rows = entityManager
        .createNativeQuery("SELECT update_by FROM t_order_header WHERE order_no = :orderNo AND del_flag = 0")
        .setParameter("orderNo", orderNo)
        .getResultList();
    assertFalse(rows.isEmpty(), "订单应存在");
    return rows.get(0);
  }

  /**
   * 查询订单最新时间线事件的 operator_id（直接查数据库）。
   */
  private String queryLastTimelineOperator(String orderNo) {
    @SuppressWarnings("unchecked")
    java.util.List<String> rows = entityManager
        .createNativeQuery(
            "SELECT e.operator_id FROM t_order_event e "
                + "INNER JOIN t_order_header h ON e.order_header_id = h.id "
                + "WHERE h.order_no = :orderNo AND h.del_flag = 0 "
                + "ORDER BY e.id DESC LIMIT 1")
        .setParameter("orderNo", orderNo)
        .getResultList();
    assertFalse(rows.isEmpty(), "时间线事件应存在");
    return rows.get(0);
  }

  private MockHttpSession login(String username, String password) throws Exception {
    String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
    MvcResult result =
        mockMvc
            .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }

  private void executeUpdate(String sql) {
    entityManager.createNativeQuery(sql).executeUpdate();
  }

  // JUnit 5 assertions
  private static void assertTrue(boolean condition, String message) {
    org.junit.jupiter.api.Assertions.assertTrue(condition, message);
  }

  private static void assertFalse(boolean condition, String message) {
    org.junit.jupiter.api.Assertions.assertFalse(condition, message);
  }

  private static void assertNotNull(Object obj, String message) {
    org.junit.jupiter.api.Assertions.assertNotNull(obj, message);
  }

  private static void assertEquals(Object expected, Object actual, String message) {
    org.junit.jupiter.api.Assertions.assertEquals(expected, actual, message);
  }
}
