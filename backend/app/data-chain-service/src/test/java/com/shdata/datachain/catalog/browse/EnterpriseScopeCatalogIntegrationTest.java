package com.shdata.datachain.catalog.browse;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.shdata.datachain.common.port.ChainAttestationPort;
import com.shdata.datachain.entity.EnterpriseEntity;
import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import com.shdata.datachain.service.security.UserAccountService;
import java.nio.charset.StandardCharsets;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * TASK-WSC-905：mine 本企业、全链无企业 filter、ADMIN 写本企业、IDOR 负例、空 create_by。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_ent_905;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class EnterpriseScopeCatalogIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CatalogBrowseSeedStore catalog;
  @Autowired private UserAccountService users;
  @MockBean private ChainAttestationPort attestationPort;

  @Test
  @DisplayName("905-mine-enterprise-scope")
  void mine_enterpriseScope_sameEnterpriseVisible_crossHidden() throws Exception {
    stubAttest();
    MockHttpSession admin = login("admin", "demo");
    MockHttpSession admin2 = login("admin2", "demo");
    MockHttpSession provider = login("provider", "demo");
    MockHttpSession foreign = createForeignProvider(admin, "xprov905a");

    String adminCode = "ADM-MNE-9051";
    String admin2Code = "AD2-MNE-9052";
    String providerCode = "PRV-MNE-9053";
    String foreignCode = "FRN-MNE-9054";
    createProduct(admin, adminCode, "同企管理员产品");
    createProduct(admin2, admin2Code, "同企另一管理员产品");
    createProduct(provider, providerCode, "同企提供方产品");
    createProduct(foreign, foreignCode, "跨企产品");

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(admin)
                .param("mine", "true")
                .param("pageSize", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[*].productCode", hasItem(adminCode)))
        .andExpect(jsonPath("$.data.items[*].productCode", hasItem(admin2Code)))
        .andExpect(jsonPath("$.data.items[*].productCode", hasItem(providerCode)))
        .andExpect(jsonPath("$.data.items[*].productCode", everyItem(not(foreignCode))));

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(provider)
                .param("mine", "true")
                .param("pageSize", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[*].productCode", hasItem(adminCode)))
        .andExpect(jsonPath("$.data.items[*].productCode", hasItem(providerCode)))
        .andExpect(jsonPath("$.data.items[*].productCode", everyItem(not(foreignCode))));

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(foreign)
                .param("mine", "true")
                .param("pageSize", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[*].productCode", hasItem(foreignCode)))
        .andExpect(jsonPath("$.data.items[*].productCode", everyItem(not(adminCode))));
  }

  @Test
  void user_mineWriteImportMaintenance_403() throws Exception {
    stubAttest();
    MockHttpSession user = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/products").session(user).param("mine", "true"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(user)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productCode\":\"USR-WRT-9050\",\"productName\":\"用户不可写\",\"productType\":\"OTHER\",\"industryCategory\":\"卫生和社会工作\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
    mockMvc
        .perform(
            multipart("/api/v1/catalog/products/import")
                .file(new MockMultipartFile("file", "x.csv", "text/csv", "a,b".getBytes(StandardCharsets.UTF_8)))
                .session(user))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries").param("scope", "myCatalog").session(user))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));
  }

  @Test
  void fullChain_crossEnterpriseParity_andIgnoresClientEnterpriseQuery() throws Exception {
    stubAttest();
    MockHttpSession admin = login("admin", "demo");
    MockHttpSession foreign = createForeignProvider(admin, "xprov905b");
    createProduct(admin, "ADM-ALL-9055", "全链可见甲");
    createProduct(foreign, "FRN-ALL-9056", "全链可见乙");

    Set<String> adminCodes = listProductCodes(admin, false, null);
    Set<String> foreignCodes = listProductCodes(foreign, false, "1");
    assertEquals(adminCodes, foreignCodes);
    assertEquals(true, adminCodes.contains("ADM-ALL-9055"));
    assertEquals(true, adminCodes.contains("FRN-ALL-9056"));

    String unassignedId =
        String.valueOf(
            users.findEnterpriseByCode(EnterpriseEntity.CODE_UNASSIGNED).orElseThrow().getId());
    Set<String> tampered =
        listProductCodes(admin, true, unassignedId);
    assertFalse(tampered.contains("FRN-ALL-9056"), "篡改 enterpriseId 不得扩大 mine 可见面");
    assertEquals(true, tampered.contains("ADM-ALL-9055"));
  }

  @Test
  void admin_writeOwnEnterprise200_crossAndEmpty403_providerForeign403() throws Exception {
    stubAttest();
    MockHttpSession admin = login("admin", "demo");
    MockHttpSession admin2 = login("admin2", "demo");
    MockHttpSession provider = login("provider", "demo");
    MockHttpSession foreign = createForeignProvider(admin, "xprov905c");

    String peerId = createProduct(admin2, "AD2-WRT-9057", "同企可被管理员改");
    String foreignId = createProduct(foreign, "FRN-WRT-9058", "跨企不可改");
    String emptyId = createProduct(provider, "EMP-WRT-9059", "空归属");
    catalog.forceSetCreateBy(emptyId, "");
    String adminOwnId = createProduct(admin, "ADM-WRT-9060", "管理员自己的");

    mockMvc
        .perform(
            put("/api/v1/catalog/products/" + peerId)
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeBody("AD2-WRT-9057", "同企已改"))
                .param("enterpriseId", "999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));

    mockMvc
        .perform(
            put("/api/v1/catalog/products/" + foreignId)
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeBody("FRN-WRT-9058", "跨企冒改")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));

    mockMvc
        .perform(
            put("/api/v1/catalog/products/" + emptyId)
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeBody("EMP-WRT-9059", "空归属冒领")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));

    mockMvc
        .perform(
            put("/api/v1/catalog/products/" + adminOwnId)
                .session(provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeBody("ADM-WRT-9060", "提供方改他人")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
  }

  @Test
  void query_hasNoEnterpriseNameFilter() throws Exception {
    stubAttest();
    MockHttpSession admin = login("admin", "demo");
    createProduct(admin, "NAM-QRY-9051", "无企业名筛");
    MvcResult baseline =
        mockMvc
            .perform(get("/api/v1/catalog/products").session(admin).param("q", "NAM-QRY-9051"))
            .andExpect(status().isOk())
            .andReturn();
    MvcResult withName =
        mockMvc
            .perform(
                get("/api/v1/catalog/products")
                    .session(admin)
                    .param("q", "NAM-QRY-9051")
                    .param("enterpriseName", "不存在的企业"))
            .andExpect(status().isOk())
            .andReturn();
    List<String> a = JsonPath.read(baseline.getResponse().getContentAsString(), "$.data.items[*].productCode");
    List<String> b = JsonPath.read(withName.getResponse().getContentAsString(), "$.data.items[*].productCode");
    assertEquals(a, b);
  }

  private Set<String> listProductCodes(MockHttpSession session, boolean mine, String enterpriseId)
      throws Exception {
    var req =
        get("/api/v1/catalog/products").session(session).param("pageSize", "100");
    if (mine) {
      req = req.param("mine", "true");
    }
    if (enterpriseId != null) {
      req = req.param("enterpriseId", enterpriseId).param("enterpriseName", "伪造企业");
    }
    MvcResult result = mockMvc.perform(req).andExpect(status().isOk()).andReturn();
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
                    .content(writeBody(code, name)))
            .andExpect(status().isOk())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
  }

  private static String writeBody(String code, String name) {
    return """
        {
          "productCode":"%s",
          "productName":"%s",
          "productType":"OTHER",
          "industryCategory":"卫生和社会工作"
        }
        """
        .formatted(code, name);
  }

  private void stubAttest() {
    Mockito.when(attestationPort.attest(Mockito.any()))
        .thenAnswer(
            inv ->
                new ChainAttestationPort.AttestationResult(
                    "hash-905",
                    "did:905",
                    java.time.Instant.parse("2026-08-17T04:00:00Z"),
                    new ChainAttestationPort.AttestationResult.Certificate("模拟权属方-905")));
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
