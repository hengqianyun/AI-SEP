package com.wsc.chain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 上链列表/快照集成：最新优先、按 versionId 读快照、空态、需登录。
 */
@SpringBootTest(
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
    })
@AutoConfigureMockMvc
class ChainApiIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ChainAttestationPort chainAttestationPort;

  @Test
  void portIsExposedAsSpringBean() {
    org.junit.jupiter.api.Assertions.assertNotNull(chainAttestationPort);
    org.junit.jupiter.api.Assertions.assertTrue(
        chainAttestationPort instanceof SimulatedChainAttestationPort);
  }

  @Test
  void listVersions_requiresLogin() throws Exception {
    mockMvc
        .perform(get("/api/v1/chain/products/" + InMemoryChainStore.SEED_PRODUCT_ID + "/versions"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("401"));
  }

  @Test
  void listVersions_seedProduct_latestFirst() throws Exception {
    MockHttpSession session = login("admin", "demo");
    mockMvc
        .perform(
            get("/api/v1/chain/products/" + InMemoryChainStore.SEED_PRODUCT_ID + "/versions")
                .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items.length()").value(3))
        .andExpect(jsonPath("$.data.items[0].versionNo").value(3))
        .andExpect(jsonPath("$.data.items[1].versionNo").value(2))
        .andExpect(jsonPath("$.data.items[2].versionNo").value(1))
        .andExpect(jsonPath("$.data.items[0].metadataHash").isNotEmpty())
        .andExpect(jsonPath("$.data.items[0].ownerDID").isNotEmpty())
        .andExpect(jsonPath("$.data.items[0].timestamp").isNotEmpty())
        .andExpect(jsonPath("$.data.items[0].certificate.owner").isNotEmpty());
  }

  @Test
  void listVersions_unknownProduct_returnsEmptyList() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/chain/products/prod-does-not-exist/versions").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items.length()").value(0));
  }

  @Test
  void getSnapshot_byVersionId() throws Exception {
    MockHttpSession session = login("provider", "demo");
    mockMvc
        .perform(get("/api/v1/chain/versions/cv-seed-001-v2/snapshot").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.versionId").value("cv-seed-001-v2"))
        .andExpect(jsonPath("$.data.productCode").value("WSC-DEMO-001"))
        .andExpect(jsonPath("$.data.productName").isNotEmpty())
        .andExpect(jsonPath("$.data.productType").value("DATASET"))
        .andExpect(jsonPath("$.data.capturedAt").isNotEmpty())
        .andExpect(jsonPath("$.data.attestation.metadataHash").isNotEmpty())
        .andExpect(jsonPath("$.data.attestation.ownerDID").isNotEmpty())
        .andExpect(jsonPath("$.data.attestation.certificate.owner").isNotEmpty());
  }

  @Test
  void getSnapshot_unknownVersion_returns404() throws Exception {
    MockHttpSession session = login("admin", "demo");
    mockMvc
        .perform(get("/api/v1/chain/versions/cv-missing/snapshot").session(session))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("404"));
  }

  private MockHttpSession login(String username, String password) throws Exception {
    var result =
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
