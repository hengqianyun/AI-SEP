package com.shdata.datachain.catalog.browse;

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
 * TASK-WSC-603 P0：mine 过滤 + create_by 空/异主 403 + 本人 200。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_mine_603;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class CreateByOwnershipIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CatalogBrowseSeedStore catalog;
  @MockBean private ChainAttestationPort attestationPort;

  @Test
  void mineTrue_listsOnlyOwnCreateBy() throws Exception {
    stubAttest();
    MockHttpSession provider = login("provider", "demo");
    createProduct(provider, "OWN-MINE-6032", "本人产品B");

    String foreignId = createProduct(provider, "OWN-MINE-6031", "将改为异主");
    catalog.forceSetCreateBy(foreignId, "other-user-id");

    mockMvc
        .perform(get("/api/v1/catalog/products").session(provider).param("mine", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items[*].productCode", hasItem("OWN-MINE-6032")))
        .andExpect(jsonPath("$.data.items[*].productCode", everyItem(not("OWN-MINE-6031"))));
  }

  @Test
  void provider_updateOwn_200_emptyAndOther_403() throws Exception {
    stubAttest();
    MockHttpSession provider = login("provider", "demo");
    String ownId = createProduct(provider, "OWN-UPD-6033", "可编辑本人");

    mockMvc
        .perform(
            put("/api/v1/catalog/products/" + ownId)
                .session(provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"OWN-UPD-6033",
                      "productName":"本人已改",
                      "productType":"OTHER",
                      "industryCategory":"卫生和社会工作"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));

    String emptyOwnerId = createProduct(provider, "OWN-EMP-6034", "空归属将被清");
    catalog.forceSetCreateBy(emptyOwnerId, "");
    mockMvc
        .perform(
            put("/api/v1/catalog/products/" + emptyOwnerId)
                .session(provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"OWN-EMP-6034",
                      "productName":"冒领空归属",
                      "productType":"OTHER",
                      "industryCategory":"卫生和社会工作"
                    }
                    """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));

    String otherId = createProduct(provider, "OWN-OTH-6035", "异主产品");
    catalog.forceSetCreateBy(otherId, "someone-else");
    mockMvc
        .perform(
            put("/api/v1/catalog/products/" + otherId)
                .session(provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"OWN-OTH-6035",
                      "productName":"改他人",
                      "productType":"OTHER",
                      "industryCategory":"卫生和社会工作"
                    }
                    """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
  }

  @Test
  void admin_productWriteForbidden_user_productWriteForbidden() throws Exception {
    MockHttpSession admin = login("admin", "demo");
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"ADM-WRT-6036",
                      "productName":"管理员不可写",
                      "productType":"OTHER",
                      "industryCategory":"卫生和社会工作"
                    }
                    """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));

    MockHttpSession user = login("user", "demo");
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(user)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"USR-WRT-6037",
                      "productName":"普通用户不可写",
                      "productType":"OTHER",
                      "industryCategory":"卫生和社会工作"
                    }
                    """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
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
                    "hash-603",
                    "did:603",
                    java.time.Instant.parse("2026-08-05T10:00:00Z"),
                    new ChainAttestationPort.AttestationResult.Certificate("模拟权属方-603")));
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
