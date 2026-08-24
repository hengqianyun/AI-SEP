package com.shdata.datachain.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import org.springframework.transaction.support.TransactionTemplate;
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
 * TASK-WSC-918 集成测试门禁：数字合约版本。
 *
 * <p>用例覆盖：918-contract-versions。
 * 验收：创建后详情可见版本 1；后续状态变更版本递增且历史可查；无主数据空值不阻断下单。
 * 环境：H2 in-memory（MODE=MySQL），Flyway 关闭，ddl-auto=create-drop。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_order_918;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
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
class ContractVersionIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private EntityManager entityManager;
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private com.shdata.datachain.common.security.PasswordHasher passwordHasher;

  private static final String LOGIN_URL = "/api/v1/auth/session";
  private static final String ORDERS_URL = "/api/v1/orders";

  private Long adminUserId;
  private Long providerUserId;
  private Long user1Id;
  private Long productId;

  @BeforeEach
  void setUp() {
    transactionTemplate.executeWithoutResult(status -> {
      // 创建 t_order_contract_version 表（OrderService 使用 EntityManager 直写）
      try {
        executeUpdate("CREATE TABLE IF NOT EXISTS t_order_contract_version ("
            + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
            + "order_header_id BIGINT NOT NULL, "
            + "version_no INT NOT NULL DEFAULT 0, "
            + "snapshot_json CLOB, "
            + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "create_by VARCHAR(64) DEFAULT '', "
            + "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "update_by VARCHAR(64) DEFAULT '', "
            + "del_flag TINYINT DEFAULT 0"
            + ")");
      } catch (Exception e) {
        // ignore if already exists
      }

      // 清理种子数据
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
      user1Id = insertUser("testuser1", "需求方张三", 1L, "USER");

      // 产品
      productId = insertProduct("P-918-001", "测试数据产品", "DATA_SERVICE", providerUserId.toString(), true);
    });
  }

  // ================================================================
  // 918-contract-versions：数字合约版本
  // ================================================================

  @Nested
  @DisplayName("918-contract-versions")
  class ContractVersions {

    @Test
    @DisplayName("创建后详情可见版本 1")
    void createOrder_thenDetailHasVersion1() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "版本测试");

      MvcResult detail = mockMvc
          .perform(get(ORDERS_URL + "/" + orderId).session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code").value("0"))
          .andExpect(jsonPath("$.data.contractVersions.length()").value(1))
          .andExpect(jsonPath("$.data.contractVersions[0].versionNo").value(1))
          .andReturn();

      // 版本快照不应为空
      String snapshotJson = JsonPath.read(
          detail.getResponse().getContentAsString(),
          "$.data.contractVersions[0].snapshotJson");
      assertNotNull(snapshotJson, "合约快照不应为空");
      assertFalse(snapshotJson.isEmpty(), "合约快照不应为空字符串");
    }

    @Test
    @DisplayName("确认订单后版本递增为 2")
    void confirmOrder_versionIncrements() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "版本递增测试");

      MockHttpSession providerSession = login("testprovider", "demo");
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession))
          .andExpect(status().isOk());

      mockMvc
          .perform(get(ORDERS_URL + "/" + orderId).session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.contractVersions.length()").value(2))
          .andExpect(jsonPath("$.data.contractVersions[1].versionNo").value(2));
    }

    @Test
    @DisplayName("主路径达成后版本 ≥ 4（创建+确认+提交+确认合约）")
    void mainPath_contractReached_versionAtLeast4() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "主路径版本测试");

      MockHttpSession providerSession = login("testprovider", "demo");

      // 确认订单
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/confirm").session(providerSession))
          .andExpect(status().isOk());

      // 提交合约
      mockMvc.perform(buildContractMultipart(orderId, userSession))
          .andExpect(status().isOk());

      // 确认合约
      String confirmBody = "{\"noticeAccepted\":true}";
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/contract/confirm")
              .session(providerSession)
              .contentType(MediaType.APPLICATION_JSON)
              .content(confirmBody))
          .andExpect(status().isOk());

      // 验证版本列表
      mockMvc
          .perform(get(ORDERS_URL + "/" + orderId).session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.contractVersions.length()").value(4))
          .andExpect(jsonPath("$.data.contractVersions[0].versionNo").value(1))
          .andExpect(jsonPath("$.data.contractVersions[3].versionNo").value(4));
    }

    @Test
    @DisplayName("取消后版本也递增")
    void cancelOrder_versionIncrements() throws Exception {
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "取消版本测试");

      // 创建后版本 1
      mockMvc
          .perform(get(ORDERS_URL + "/" + orderId).session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.contractVersions.length()").value(1));

      // 取消后版本 2
      mockMvc.perform(post(ORDERS_URL + "/" + orderId + "/cancel").session(userSession))
          .andExpect(status().isOk());

      mockMvc
          .perform(get(ORDERS_URL + "/" + orderId).session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.contractVersions.length()").value(2))
          .andExpect(jsonPath("$.data.contractVersions[1].versionNo").value(2));
    }

    @Test
    @DisplayName("无主数据空值不阻断下单")
    void createOrder_noBlockingOnEmptyFields() throws Exception {
      // 即使产品某些字段为空，订单仍应创建成功并生成版本 1
      MockHttpSession userSession = login("testuser1", "demo");
      String orderId = createOrder(userSession, productId, "空值不阻断");

      mockMvc
          .perform(get(ORDERS_URL + "/" + orderId).session(userSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.contractVersions.length()").value(1))
          .andExpect(jsonPath("$.data.contractVersions[0].versionNo").value(1));
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
    java.util.List<Number> ids = entityManager
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
    java.util.List<Number> ids = entityManager
        .createNativeQuery("SELECT id FROM t_data_product WHERE product_code = :code AND del_flag = 0")
        .setParameter("code", code)
        .getResultList();
    Long id = ids.get(0).longValue();

    entityManager.flush();
    entityManager.clear();

    return id;
  }

  private String createOrder(MockHttpSession session, Long productId, String remark) throws Exception {
    String body = String.format(
        "{\"productId\":\"%d\",\"remark\":\"%s\",\"noticeAccepted\":true}", productId, remark);
    MvcResult result = mockMvc
        .perform(post(ORDERS_URL).session(session).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.data.orderId");
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
      buildContractMultipart(String orderId, MockHttpSession session) throws Exception {
    String txJson = String.format(
        "{\"orderLines\":[{\"unit\":\"条\",\"quantity\":1,\"unitPrice\":\"100.00\",\"subtotal\":\"100.00\"}],"
            + "\"amountTaxInclusive\":\"100.00\",\"amountTaxExclusive\":\"100.00\",\"currency\":\"CNY\"}");
    MockMultipartFile file = new MockMultipartFile(
        "file", "contract.pdf", "application/pdf", "fake-pdf-content".getBytes(StandardCharsets.UTF_8));
    return multipart(ORDERS_URL + "/" + orderId + "/contract")
        .file(file)
        .param("transactionInfo", txJson)
        .session(session);
  }

  private MockHttpSession login(String username, String password) throws Exception {
    String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
    MvcResult result = mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }

  private void executeUpdate(String sql) {
    entityManager.createNativeQuery(sql).executeUpdate();
  }

  private static void assertNotNull(Object obj, String message) {
    org.junit.jupiter.api.Assertions.assertNotNull(obj, message);
  }

  private static void assertFalse(boolean condition, String message) {
    org.junit.jupiter.api.Assertions.assertFalse(condition, message);
  }
}
