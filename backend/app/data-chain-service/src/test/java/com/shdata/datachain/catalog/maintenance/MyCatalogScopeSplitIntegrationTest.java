package com.shdata.datachain.catalog.maintenance;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.shdata.datachain.common.port.ChainAttestationPort;
import com.shdata.datachain.entity.EnterpriseEntity;
import com.shdata.datachain.service.security.UserAccountService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * TASK-WSC-907：ADMIN 双入口 N vs M；PROVIDER 本人；跨 scope 403。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_maint_907;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class MyCatalogScopeSplitIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserAccountService users;
  @MockBean private ChainAttestationPort attestationPort;

  @Test
  @DisplayName("907-my-catalog-scope-split")
  void adminDualEntry_fullN_myCatalogM_providerOwnOnly() throws Exception {
    stubAttest();
    MockHttpSession admin = login("admin", "demo");
    MockHttpSession admin2 = login("admin2", "demo");
    MockHttpSession provider = login("provider", "demo");
    MockHttpSession foreign = createForeignProvider(admin, "xprov907a");

    String adminCode = "ADM-MNT-9071";
    String admin2Code = "AD2-MNT-9072";
    String providerCode = "PRV-MNT-9073";
    String foreignCode = "FRN-MNT-9074";
    String foreignId = createProduct(foreign, foreignCode, "跨企维护产品");
    createProduct(admin, adminCode, "本企管理员产品");
    createProduct(admin2, admin2Code, "同企另一管理员产品");
    createProduct(provider, providerCode, "同企提供方产品");

    Set<String> fullCodes = listMaintenanceCodes(admin, "full");
    Set<String> myCodes = listMaintenanceCodes(admin, "myCatalog");
    int n = fullCodes.size();
    int m = myCodes.size();
    assertTrue(m <= n, "myCatalog M must be <= full N");
    assertTrue(fullCodes.contains(adminCode));
    assertTrue(fullCodes.contains(admin2Code));
    assertTrue(fullCodes.contains(providerCode));
    assertTrue(fullCodes.contains(foreignCode), "跨企产品只出现在 full");
    assertTrue(myCodes.contains(adminCode));
    assertTrue(myCodes.contains(admin2Code));
    assertTrue(myCodes.contains(providerCode));
    assertTrue(!myCodes.contains(foreignCode), "myCatalog 不含跨企产品");

    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries")
                .session(admin)
                .param("scope", "myCatalog")
                .param("status", "ALL")
                .param("pageSize", "100")
                .param("enterpriseId", "999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[*].productCode", everyItem(not(foreignCode))));

    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries")
                .session(provider)
                .param("scope", "myCatalog")
                .param("status", "ALL")
                .param("pageSize", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items[*].productCode", hasItem(providerCode)))
        .andExpect(jsonPath("$.data.items[*].productCode", everyItem(not(adminCode))))
        .andExpect(jsonPath("$.data.items[*].productCode", everyItem(not(foreignCode))));

    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries")
                .session(provider)
                .param("scope", "full"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));

    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/" + foreignId)
                .session(admin)
                .param("scope", "myCatalog")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-emr-desense\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));

    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/" + foreignId)
                .session(admin)
                .param("scope", "full")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-emr-desense\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
  }

  private Set<String> listMaintenanceCodes(MockHttpSession session, String scope) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/api/v1/catalog/maintenance/entries")
                    .session(session)
                    .param("scope", scope)
                    .param("status", "ALL")
                    .param("pageSize", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andReturn();
    List<String> codes =
        JsonPath.read(result.getResponse().getContentAsString(), "$.data.items[*].productCode");
    return new HashSet<>(codes);
  }

  private MockHttpSession createForeignProvider(MockHttpSession admin, String username)
      throws Exception {
    String unassignedId =
        String.valueOf(
            users.findEnterpriseByCode(EnterpriseEntity.CODE_UNASSIGNED).orElseThrow().getId());
    mockMvc
        .perform(
            post("/api/v1/admin/users")
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"username\":\""
                        + username
                        + "\",\"password\":\"demo\",\"role\":\"PROVIDER\",\"displayName\":\"跨企提供方\",\"enterpriseId\":\""
                        + unassignedId
                        + "\"}"))
        .andExpect(status().isOk());
    return login(username, "demo");
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
                    "hash-907",
                    "did:907",
                    java.time.Instant.parse("2026-08-17T05:00:00Z"),
                    new ChainAttestationPort.AttestationResult.Certificate("模拟权属方-907")));
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
