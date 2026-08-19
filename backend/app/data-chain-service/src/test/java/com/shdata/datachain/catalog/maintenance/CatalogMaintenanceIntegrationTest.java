package com.shdata.datachain.catalog.maintenance;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.shdata.datachain.common.port.ChainAttestationPort;
import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import com.shdata.datachain.repository.InMemoryChainStore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * REQ-CAT-007 / TASK-WSC-607：ADMIN 全量；PROVIDER 仅 create_by=本人；USER 403。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_maint_607;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class CatalogMaintenanceIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CatalogBrowseSeedStore catalog;
  @Autowired private InMemoryChainStore chainStore;
  @MockBean private ChainAttestationPort attestationPort;

  @Test
  void pending_to_maintained_singleAssociate() throws Exception {
    MockHttpSession admin = login("admin", "demo");

    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries?status=PENDING&page=1&pageSize=20").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.data.items[0].maintenanceStatus").value("PENDING"));

    // 路径可按 productCode（JPA 后旧自定义 id 不可用）
    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/PEND-0001")
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-txn-retail\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.maintenanceStatus").value("MAINTAINED"))
        .andExpect(jsonPath("$.data.l3CategoryId").value("cat-l3-txn-retail"))
        .andExpect(jsonPath("$.data.categoryPath").isNotEmpty());

    org.assertj.core.api.Assertions.assertThat(chainStore.listByProductId("PEND-0001"))
        .hasSize(1);

    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries?status=PENDING").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[?(@.productCode=='PEND-0001')]").isEmpty());
  }

  @Test
  void batchAssociate_and_leafRequired() throws Exception {
    MockHttpSession admin = login("admin", "demo");

    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/PEND-0002")
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l2-emr\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ERR_CATEGORY_LEAF_REQUIRED"));

    mockMvc
        .perform(
            post("/api/v1/catalog/maintenance/entries/batch")
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productIds\":[\"PEND-0002\",\"PEND-0003\",\"missing-x\"],"
                        + "\"l3CategoryId\":\"cat-l3-emr-struct\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.successCount").value(2))
        .andExpect(jsonPath("$.data.failureCount").value(1));
  }

  @Test
  void pagination_pagePageSizeTotal() throws Exception {
    MockHttpSession admin = login("admin", "demo");

    // H2 无 Flyway 时仅有 ensurePendingSamples 演示条；用 pageSize=2 验证分页字段
    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries?status=ALL&page=1&pageSize=2").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(2))
        .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
        .andExpect(jsonPath("$.data.items.length()").value(2));

    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries?status=ALL&page=2&pageSize=2").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page").value(2))
        .andExpect(jsonPath("$.data.items.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
  }

  @Test
  void user_forbidden_maintenance() throws Exception {
    MockHttpSession user = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries").session(user))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));

    mockMvc
        .perform(
            post("/api/v1/catalog/maintenance/entries/batch")
                .session(user)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productIds\":[\"PEND-0001\"],\"l3CategoryId\":\"cat-l3-emr-desense\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));
  }

  @Test
  void provider_listOnlyOwn_and_associateOwn200_other403() throws Exception {
    stubAttest();
    MockHttpSession provider = login("provider", "demo");
    String ownId = createProduct(provider, "MAINT-OWN-6071", "本人待维护");
    String foreignId = createProduct(provider, "MAINT-OTH-6072", "将被改为异主");
    catalog.forceSetCreateBy(foreignId, "other-user-id");
    String emptyId = createProduct(provider, "MAINT-EMP-6073", "空归属");
    catalog.forceSetCreateBy(emptyId, "");

    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries?status=ALL").session(provider))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items[*].productCode", hasItem("MAINT-OWN-6071")))
        .andExpect(jsonPath("$.data.items[*].productCode", everyItem(not("MAINT-OTH-6072"))))
        .andExpect(jsonPath("$.data.items[*].productCode", everyItem(not("MAINT-EMP-6073"))));

    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/" + ownId)
                .session(provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-emr-desense\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.maintenanceStatus").value("MAINTAINED"))
        .andExpect(jsonPath("$.data.l3CategoryId").value("cat-l3-emr-desense"));

    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/" + foreignId)
                .session(provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-emr-struct\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));

    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/" + emptyId)
                .session(provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-emr-struct\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));

    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/missing-no-such")
                .session(provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-emr-struct\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("404"));
  }

  @Test
  void provider_batch_ownOk_foreignInFailures() throws Exception {
    stubAttest();
    MockHttpSession provider = login("provider", "demo");
    String ownId = createProduct(provider, "MAINT-BAT-6074", "批量本人");
    String foreignId = createProduct(provider, "MAINT-BAT-6075", "批量异主");
    catalog.forceSetCreateBy(foreignId, "someone-else");

    mockMvc
        .perform(
            post("/api/v1/catalog/maintenance/entries/batch")
                .session(provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productIds\":[\""
                        + ownId
                        + "\",\""
                        + foreignId
                        + "\"],"
                        + "\"l3CategoryId\":\"cat-l3-txn-retail\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.successCount").value(1))
        .andExpect(jsonPath("$.data.failureCount").value(1))
        .andExpect(jsonPath("$.data.failures[0].productId").value(foreignId))
        .andExpect(jsonPath("$.data.failures[0].reasonCode").value("ERR_FORBIDDEN"));

    org.assertj.core.api.Assertions.assertThat(chainStore.listByProductId("MAINT-BAT-6074"))
        .hasSize(2);
    org.assertj.core.api.Assertions.assertThat(chainStore.listByProductId("MAINT-BAT-6075"))
        .hasSize(1);
  }

  private String createProduct(MockHttpSession session, String code, String name) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/catalog/products")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "productCode":"%s",
                          "productName":"%s",
                          "productType":"OTHER",
                          "industryCategory":"卫生和社会工作"
                        }
                        """
                            .formatted(code, name)))
            .andExpect(status().isOk())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
  }

  private void stubAttest() {
    Mockito.when(attestationPort.attest(Mockito.any()))
        .thenAnswer(
            inv ->
                new ChainAttestationPort.AttestationResult(
                    "hash-607",
                    "did:607",
                    java.time.Instant.parse("2026-08-06T08:00:00Z"),
                    new ChainAttestationPort.AttestationResult.Certificate("模拟权属方-607")));
  }

  private MockHttpSession login(String username, String password) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/session")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }
}
