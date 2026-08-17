package com.shdata.datachain.catalog.maintenance;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.shdata.datachain.common.port.ChainAttestationPort;
import org.junit.jupiter.api.DisplayName;
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
 * TASK-WSC-905：maintenance interceptor scope-aware（PROVIDER full→403 / myCatalog→200）。
 *
 * <p>ADMIN myCatalog 结果集本企业过滤交 TASK-WSC-907（本任务仅保证 200/403）。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_maint_scope_905;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "spring.flyway.enabled=false",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
    })
@AutoConfigureMockMvc
class MaintenanceScopeIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private ChainAttestationPort attestationPort;

  @Test
  @DisplayName("905-provider-maintenance-403")
  void provider_scopeFull_403_myCatalog_200() throws Exception {
    stubAttest();
    MockHttpSession provider = login("provider", "demo");
    String ownId = createProduct(provider, "MAI-OWN-9051", "本人维护条目");

    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries")
                .session(provider)
                .param("scope", "full")
                .param("enterpriseId", "1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));

    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/" + ownId)
                .session(provider)
                .param("scope", "full")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-emr-desense\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));

    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries")
                .session(provider)
                .param("scope", "myCatalog")
                .param("status", "ALL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items[*].productCode", hasItem("MAI-OWN-9051")));
  }

  @Test
  void admin_scopeFullAndMyCatalog_200() throws Exception {
    MockHttpSession admin = login("admin", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries").session(admin).param("scope", "full"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries").session(admin).param("scope", "myCatalog"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
  }

  @Test
  void user_anyScope_403() throws Exception {
    MockHttpSession user = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries").session(user).param("scope", "full"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));
    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries").session(user).param("scope", "myCatalog"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));
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
                    "hash-905m",
                    "did:905m",
                    java.time.Instant.parse("2026-08-17T04:10:00Z"),
                    new ChainAttestationPort.AttestationResult.Certificate("模拟权属方-905m")));
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
